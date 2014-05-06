package com.example.renderscripttexturecompressor.pkm;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.opengl.ETC1;
import android.opengl.ETC1Util.ETC1Texture;
import android.support.v8.renderscript.RenderScript;

import com.example.renderscripttexturecompressor.etc1.java.JavaETC1;
import com.example.renderscripttexturecompressor.etc1.rs.RsETC1;
import com.example.renderscripttexturecompressor.etc1.rs.ScriptC_etc1compressor;

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

	public static ETC1Texture encodeTextureAsETC1_Rs(InputStream stream, RenderScript rs, ScriptC_etc1compressor script)
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

			final int encodedImageSize = RsETC1.getEncodedDataSize(
					bitmap.getWidth(), bitmap.getHeight());
			ByteBuffer compressedImage = ByteBuffer.allocateDirect(
					encodedImageSize).order(ByteOrder.nativeOrder());
			// RGB_565 is 2 bytes per pixel

			RsETC1.encodeImage(rs, script, buffer, bitmap.getWidth(), bitmap.getHeight(),
					2, 2 * bitmap.getWidth(), compressedImage);

			ETC1Texture texture = new ETC1Texture(bitmap.getWidth(),
					bitmap.getHeight(), compressedImage);

			return texture;
		}
		return null;
	}
}
