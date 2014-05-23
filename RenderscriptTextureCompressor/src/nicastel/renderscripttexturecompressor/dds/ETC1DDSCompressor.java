package nicastel.renderscripttexturecompressor.dds;

import gov.nasa.worldwind.util.ImageUtil;
import gov.nasa.worldwind.util.dds.DDSCompressor;
import gov.nasa.worldwind.util.dds.DDSConstants;
import gov.nasa.worldwind.util.dds.DDSHeader;
import gov.nasa.worldwind.util.dds.DXT1Compressor;
import gov.nasa.worldwind.util.dds.DXT3Compressor;
import gov.nasa.worldwind.util.dds.DXTCompressionAttributes;
import gov.nasa.worldwind.util.dds.DXTCompressor;

import java.nio.ByteBuffer;

import nicastel.renderscripttexturecompressor.etc1.rs.RsETC1;
import android.graphics.Bitmap;

public class ETC1DDSCompressor extends DDSCompressor {
    protected DXTCompressor getDXTCompressor(Bitmap image, DXTCompressionAttributes attributes)
    {
        // If the caller specified a DXT format in the attributes, then we return a compressor matching that format.
        // Otherwise, we choose one automatically from the image type. If no choice can be made from the image type,
        // we default to using a DXT3 compressor.

        if (attributes.getDXTFormat() == DDSConstants.D3DFMT_DXT1)
        {
            return new DXT1Compressor();
        }
        else if (attributes.getDXTFormat() == ETCConstants.D3DFMT_ETC1)
        {
            return new ETC1Compressor();
        }
        else if (attributes.getDXTFormat() == DDSConstants.D3DFMT_DXT2
            || attributes.getDXTFormat() == DDSConstants.D3DFMT_DXT3)
        {
            return new DXT3Compressor();
        }
        else if (!image.hasAlpha())
        {
            return new DXT1Compressor();
        }
        else
        {
            return new DXT3Compressor();
        }
    }
    
	protected Bitmap[] buildMipMaps(Bitmap image, DXTCompressionAttributes attributes) {
		// Build the mipmap chain using a premultiplied alpha image format. This is necessary to ensure that
		// transparent colors do not bleed into the opaque colors. For example, without premultiplied alpha the colors
		// in a totally transparent pixel may contribute when one mipmap level is filtered (with either a box or a
		// bilinear filter) to produce the pixels for the next level.
		//
		// The DXT color block extractor typically accessed Bitmap data via a call to getRGB(). This returns
		// a packed 8888 ARGB int, where the color components are known to be not premultiplied, and in the sRGB color
		// space. Therefore computing mipmaps in this way does not affect the rest of the DXT pipeline, unless color
		// data is accessed directly. In this case, such code would be responsible for recognizing the color model
		// (premultiplied) and behaving accordingly.

		Bitmap.Config mipmapImageType = Bitmap.Config.RGB_565;
		int maxLevel = ImageUtil.getMaxMipmapLevel(image.getWidth(), image.getHeight());

		if(attributes.getDXTFormat() == ETCConstants.D3DFMT_ETC1) {
			// mipmaps are computed with renderscript API
			Bitmap [] arr = {image};
			return arr;
		} else {
			return ImageUtil.buildMipmaps(image, mipmapImageType, maxLevel);
		}
		
	}
	
	@Override
	protected ByteBuffer doCompressImage(DXTCompressor compressor, Bitmap image, DXTCompressionAttributes attributes) {
		// Create the DDS header structure that describes the specified image, compressor, and compression attributes.
		DDSHeader header = this.createDDSHeader(compressor, image, attributes);

		// Compute the DDS file size and mip map levels. If the attributes specify to build mip maps, then we compute
		// the total file size including mip maps, create a chain of mip map images, and update the DDS header to
		// describe the number of mip map levels. Otherwise, we compute the file size for a single image and do nothing
		// to the DDS header.
		Bitmap[] mipMapLevels = null;
		int fileSize = 4 + header.getSize();

		if (attributes.isBuildMipmaps()) {			
			mipMapLevels = this.buildMipMaps(image, attributes);
			
			int maxLevel = ImageUtil.getMaxMipmapLevel(image.getWidth(), image.getHeight());
			
			if(attributes.getDXTFormat() == ETCConstants.D3DFMT_ETC1) {
				// mipmaps are computed with renderscript API
				int width = image.getWidth();
				int height = image.getHeight();
				for (int i = 1; i < maxLevel; i++) {					
					fileSize += RsETC1.getEncodedDataSize(width, height);
					width /= 2;
					height /= 2;
				}
				System.out.println("fileSize : "+fileSize);
			} else {
				for (Bitmap mipMapImage : mipMapLevels) {
					fileSize += compressor.getCompressedSize(mipMapImage, attributes);
				}
			}
			
			header.setFlags(header.getFlags() | DDSConstants.DDSD_MIPMAPCOUNT);
			header.setMipMapCount(1+maxLevel);
		} else {
			fileSize += compressor.getCompressedSize(image, attributes);
		}

		// Create a little endian buffer that holds the bytes of the DDS file.
		java.nio.ByteBuffer buffer = this.createBuffer(fileSize);
		buffer.order(java.nio.ByteOrder.LITTLE_ENDIAN);

		// Write the DDS magic number and DDS header to the file.
		buffer.putInt(DDSConstants.MAGIC);
		this.writeDDSHeader(header, buffer);

		// Write the compressed DXT blocks to the DDS file. If the attributes specify to build mip maps, then we write
		// each mip map level to the DDS file, starting with level 0 and ending with level N. Otherwise, we write a
		// single image to the DDS file.
		if (mipMapLevels == null) {
			compressor.compressImage(image, attributes, buffer);
		} else {
			for (Bitmap mipMapImage : mipMapLevels) {
				compressor.compressImage(mipMapImage, attributes, buffer);
			}
		}

		buffer.rewind();
		return buffer;
	}
	
}
