package nicastel.renderscripttexturecompressor.pkm;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import nicastel.renderscripttexturecompressor.etc1.java.JavaETC1;
import nicastel.renderscripttexturecompressor.etc1.rs.RsETC1;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.opengl.ETC1;
import android.opengl.ETC1Util.ETC1Texture;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.Allocation.MipmapControl;
import nicastel.renderscripttexturecompressor.etc1.rs.ScriptC_etc1compressor;

public class PKMEncoder {
	public static ETC1Texture encodeTextureAsETC1_Sdk(InputStream stream)
			throws IOException, FileNotFoundException {
		// stream.reset();
		// stream.mark(1024);
		Options opts = new BitmapFactory.Options();
		//opts.inPremultiplied = false;
		opts.inPreferredConfig = Config.RGB_565;
		Bitmap bitmap = BitmapFactory.decodeStream(stream, null, opts);
		if (bitmap != null) {
			ByteBuffer buffer = ByteBuffer.allocateDirect(
					bitmap.getRowBytes() * bitmap.getHeight()).order(
					ByteOrder.nativeOrder());
			bitmap.copyPixelsToBuffer(buffer);
			buffer.position(0);

			System.out.println("Width : " + bitmap.getWidth());
			System.out.println("Height : " + bitmap.getHeight());
			System.out.println("Config : " + bitmap.getConfig());

			if (bitmap.getConfig() == Bitmap.Config.ARGB_4444
					|| bitmap.getConfig() == Bitmap.Config.ARGB_8888) {
				System.out.println("Texture need aplha channel");
				return null;
			}

			final int encodedImageSize = ETC1.getEncodedDataSize(
					bitmap.getWidth(), bitmap.getHeight());
			ByteBuffer compressedImage = ByteBuffer.allocateDirect(
					encodedImageSize).order(ByteOrder.nativeOrder());
			// RGB_565 is 2 bytes per pixel

			ETC1.encodeImage(buffer, bitmap.getWidth(), bitmap.getHeight(),
					2, 2 * bitmap.getWidth(), compressedImage);

			ETC1Texture texture = new ETC1Texture(bitmap.getWidth(),
					bitmap.getHeight(), compressedImage);

			return texture;
		}
		return null;
	}
	
	public static ETC1Texture encodeTextureAsETC1_Java(InputStream stream)
			throws IOException, FileNotFoundException {
		// stream.reset();
		// stream.mark(1024);
		Options opts = new BitmapFactory.Options();
		//opts.inPremultiplied = false;
		opts.inPreferredConfig = Config.RGB_565;
		Bitmap bitmap = BitmapFactory.decodeStream(stream, null, opts);
		if (bitmap != null) {
			ByteBuffer buffer = ByteBuffer.allocateDirect(
					bitmap.getRowBytes() * bitmap.getHeight()).order(
					ByteOrder.nativeOrder());
			bitmap.copyPixelsToBuffer(buffer);
			buffer.position(0);

			System.out.println("Width : " + bitmap.getWidth());
			System.out.println("Height : " + bitmap.getHeight());
			System.out.println("Config : " + bitmap.getConfig());

			if (bitmap.getConfig() == Bitmap.Config.ARGB_4444
					|| bitmap.getConfig() == Bitmap.Config.ARGB_8888) {
				System.out.println("Texture need aplha channel");
				return null;
			}

			final int encodedImageSize = JavaETC1.getEncodedDataSize(
					bitmap.getWidth(), bitmap.getHeight());
			ByteBuffer compressedImage = ByteBuffer.allocateDirect(
					encodedImageSize).order(ByteOrder.nativeOrder());
			// RGB_565 is 2 bytes per pixel

			JavaETC1.encodeImage(buffer, bitmap.getWidth(), bitmap.getHeight(),
					2, 2 * bitmap.getWidth(), compressedImage);

			ETC1Texture texture = new ETC1Texture(bitmap.getWidth(),
					bitmap.getHeight(), compressedImage);

			return texture;
		}
		return null;
	}

	public static ETC1Texture [] encodeTextureAsETC1_Rs(InputStream stream, RenderScript rs, ScriptC_etc1compressor script)
			throws IOException, FileNotFoundException {
		// stream.reset();
		// stream.mark(1024);
		boolean hasAlpha = false;
		ByteBuffer compressedImageAlpha = null;
		Options opts = new BitmapFactory.Options();
		//opts.inPremultiplied = false;
		opts.inPreferredConfig = Config.RGB_565;
		Bitmap bitmap = BitmapFactory.decodeStream(stream, null, opts);
		if (bitmap != null) {
			System.out.println("Width : " + bitmap.getWidth());
			System.out.println("Height : " + bitmap.getHeight());
			System.out.println("Config : " + bitmap.getConfig());
			
			final int encodedImageSize = RsETC1.getEncodedDataSize(
					bitmap.getWidth(), bitmap.getHeight());

			if (bitmap.getConfig() == Bitmap.Config.ARGB_4444
					|| bitmap.getConfig() == Bitmap.Config.ARGB_8888) {
				hasAlpha = true;
				compressedImageAlpha = ByteBuffer.allocateDirect(
						encodedImageSize).order(ByteOrder.nativeOrder());
			}

			ByteBuffer compressedImage = ByteBuffer.allocateDirect(
					encodedImageSize).order(ByteOrder.nativeOrder());
			// RGB_565 is 2 bytes per pixel
			
			Allocation alloc = Allocation.createFromBitmap(rs, bitmap, MipmapControl.MIPMAP_NONE, Allocation.USAGE_SHARED);

			RsETC1.encodeImage(rs, script, alloc, bitmap.getWidth(), bitmap.getHeight(),
					2, 2 * bitmap.getWidth(), compressedImage, compressedImageAlpha, false, hasAlpha);

			ETC1Texture texture = new ETC1Texture(bitmap.getWidth(),
					bitmap.getHeight(), compressedImage);
			
			ETC1Texture textureAlpha = null;
			if(hasAlpha) {
				textureAlpha = new ETC1Texture(bitmap.getWidth(),
						bitmap.getHeight(), compressedImageAlpha);				
			}		

			alloc.destroy();
			
			ETC1Texture [] result = {texture,textureAlpha};
			
			return result;
		}
		return null;
	}
}
