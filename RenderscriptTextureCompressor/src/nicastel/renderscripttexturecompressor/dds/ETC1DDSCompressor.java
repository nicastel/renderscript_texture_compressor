package nicastel.renderscripttexturecompressor.dds;

import android.graphics.Bitmap;
import gov.nasa.worldwind.util.ImageUtil;
import gov.nasa.worldwind.util.dds.DDSCompressor;
import gov.nasa.worldwind.util.dds.DDSConstants;
import gov.nasa.worldwind.util.dds.DXT1Compressor;
import gov.nasa.worldwind.util.dds.DXT3Compressor;
import gov.nasa.worldwind.util.dds.DXTCompressionAttributes;
import gov.nasa.worldwind.util.dds.DXTCompressor;

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

		return ImageUtil.buildMipmaps(image, mipmapImageType, maxLevel);
	}
}
