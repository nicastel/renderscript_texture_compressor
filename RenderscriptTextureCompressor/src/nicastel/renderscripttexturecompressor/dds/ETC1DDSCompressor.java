package nicastel.renderscripttexturecompressor.dds;

import android.graphics.Bitmap;
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
}
