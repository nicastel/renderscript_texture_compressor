package com.example.renderscripttexturecompressor.pkm;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.ETC1;
import android.opengl.ETC1Util.ETC1Texture;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;

import com.example.renderscripttexturecompressor.etc1.JavaETC1;
import com.example.renderscripttexturecompressor.etc1.RsETC1;
import com.example.renderscripttexturecompressor.etc1.ScriptC_etc1compressor;

public class PKMEncoder {
	public static ETC1Texture encodeTextureAsETC1(InputStream stream) throws IOException,
			FileNotFoundException {
		stream.reset();
		stream.mark(1024);
		Bitmap bitmap = BitmapFactory.decodeStream(stream);
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

			RsETC1.encodeImage(buffer, bitmap.getWidth(), bitmap.getHeight(), 2,
					2 * bitmap.getWidth(), compressedImage);
			ETC1Texture texture = new ETC1Texture(bitmap.getWidth(),
					bitmap.getHeight(), compressedImage);

			// give corrupted images
			// ETC1Texture texture = ETC1Util.compressTexture(buffer,
			// bitmap.getWidth(), bitmap.getHeight(), 2, bitmap.getWidth()*2);
			
			return texture;
		}
		return null;
	}
	
	public static void testETC1compressors (RenderScript rs) {
		// Test android class (reference)
		byte[] in1 = {  6, 5, 7,
						7, 6, 5,
						9, 2, 1,
						20, 5, 80,
						75, 24, 96,
						64, 27, 43,
						45, 78, 21,
						2, 85, 32,
						9, 5, 7,
						7, 6, 5, 
						9, 2, 1, 
						85, 5, 80, 
						75, 3, 96, 
						64, 4, 43, 
						45, 78, 21, 
						2, 7, 32 };
		ByteBuffer inb = ByteBuffer.allocateDirect(48).order(
				ByteOrder.nativeOrder());
		inb.put(in1);
		ByteBuffer out = ByteBuffer.allocateDirect(8).order(
				ByteOrder.nativeOrder());

		int mask = 8;
		inb.rewind();
		ETC1.encodeBlock(inb, mask, out);
		inb.rewind();

		byte[] arrayOut1 = new byte[8];
		out.get(arrayOut1);
		
		// Test java class
		short[] in2 = {  6, 5, 7,
				7, 6, 5,
				9, 2, 1,
				20, 5, 80,
				75, 24, 96,
				64, 27, 43,
				45, 78, 21,
				2, 85, 32,
				9, 5, 7,
				7, 6, 5, 
				9, 2, 1, 
				85, 5, 80, 
				75, 3, 96, 
				64, 4, 43, 
				45, 78, 21, 
				2, 7, 32 };
		
		byte[] arrayOut2 = new byte[8];

		JavaETC1.encodeBlock(in2, mask, arrayOut2);		
			
		// Test RenderScript script
		
		byte[] in3 = {  6, 5, 7,
				7, 6, 5,
				9, 2, 1,
				20, 5, 80,
				75, 24, 96,
				64, 27, 43,
				45, 78, 21,
				2, 85, 32,
				9, 5, 7,
				7, 6, 5, 
				9, 2, 1, 
				85, 5, 80, 
				75, 3, 96, 
				64, 4, 43, 
				45, 78, 21, 
				2, 7, 32 };
		
		Allocation p00 = Allocation.createSized(rs, Element.U8_3(rs), 1); // uchar3
		Allocation p01 = Allocation.createSized(rs, Element.U8_3(rs), 1); // uchar3
		Allocation p02 = Allocation.createSized(rs, Element.U8_3(rs), 1); // uchar3
		Allocation p03 = Allocation.createSized(rs, Element.U8_3(rs), 1); // uchar3

		Allocation p10 = Allocation.createSized(rs, Element.U8_3(rs), 1); // uchar3
		Allocation p11 = Allocation.createSized(rs, Element.U8_3(rs), 1); // uchar3
		Allocation p12 = Allocation.createSized(rs, Element.U8_3(rs), 1); // uchar3
		Allocation p13 = Allocation.createSized(rs, Element.U8_3(rs), 1); // uchar3

		Allocation p20 = Allocation.createSized(rs, Element.U8_3(rs), 1); // uchar3
		Allocation p21 = Allocation.createSized(rs, Element.U8_3(rs), 1); // uchar3
		Allocation p22 = Allocation.createSized(rs, Element.U8_3(rs), 1); // uchar3
		Allocation p23 = Allocation.createSized(rs, Element.U8_3(rs), 1); // uchar3

		Allocation p30 = Allocation.createSized(rs, Element.U8_3(rs), 1); // uchar3
		Allocation p31 = Allocation.createSized(rs, Element.U8_3(rs), 1); // uchar3
		Allocation p32 = Allocation.createSized(rs, Element.U8_3(rs), 1); // uchar3
		Allocation p33 = Allocation.createSized(rs, Element.U8_3(rs), 1); // uchar3
		
		Allocation amask = Allocation.createSized(rs, Element.U32(rs), 1);
		int [] inmask = { mask };
		amask.copyFrom(inmask);
		
		//  R, G, B. Byte (3 * (x + 4 * y) is the R value of pixel (x, y)
		byte[] p00t = {in3[3 * (0 + 4 * 0)], in3[3 * (0 + 4 * 0) + 1], in3[3 * (0 + 4 * 0) + 2], 0};
		byte[] p01t = {in3[3 * (0 + 4 * 1)], in3[3 * (0 + 4 * 1) + 1], in3[3 * (0 + 4 * 1) + 2], 0};
		byte[] p02t = {in3[3 * (0 + 4 * 2)], in3[3 * (0 + 4 * 2) + 1], in3[3 * (0 + 4 * 2) + 2], 0};
		byte[] p03t = {in3[3 * (0 + 4 * 3)], in3[3 * (0 + 4 * 3) + 1], in3[3 * (0 + 4 * 2) + 3], 0};

		byte[] p10t = {in3[3 * (1 + 4 * 0)], in3[3 * (1 + 4 * 0) + 1], in3[3 * (1 + 4 * 0) + 2], 0};
		byte[] p11t = {in3[3 * (1 + 4 * 1)], in3[3 * (1 + 4 * 1) + 1], in3[3 * (1 + 4 * 1) + 2], 0};
		byte[] p12t = {in3[3 * (1 + 4 * 2)], in3[3 * (1 + 4 * 2) + 1], in3[3 * (1 + 4 * 2) + 2], 0};
		byte[] p13t = {in3[3 * (1 + 4 * 3)], in3[3 * (1 + 4 * 3) + 1], in3[3 * (1 + 4 * 3) + 2], 0};

		byte[] p20t = {in3[3 * (2 + 4 * 0)], in3[3 * (2 + 4 * 0) + 1], in3[3 * (2 + 4 * 0) + 2], 0};
		byte[] p21t = {in3[3 * (2 + 4 * 1)], in3[3 * (2 + 4 * 1) + 1], in3[3 * (2 + 4 * 1) + 2], 0};
		byte[] p22t = {in3[3 * (2 + 4 * 2)], in3[3 * (2 + 4 * 2) + 1], in3[3 * (2 + 4 * 2) + 2], 0};
		byte[] p23t = {in3[3 * (2 + 4 * 3)], in3[3 * (2 + 4 * 3) + 1], in3[3 * (2 + 4 * 3) + 2], 0};

		byte[] p30t = {in3[3 * (3 + 4 * 0)], in3[3 * (3 + 4 * 0) + 1], in3[3 * (3 + 4 * 0) + 2], 0};
		byte[] p31t = {in3[3 * (3 + 4 * 1)], in3[3 * (3 + 4 * 0) + 1], in3[3 * (3 + 4 * 1) + 2], 0};
		byte[] p32t = {in3[3 * (3 + 4 * 2)], in3[3 * (3 + 4 * 0) + 1], in3[3 * (3 + 4 * 2) + 2], 0};
		byte[] p33t = {in3[3 * (3 + 4 * 3)], in3[3 * (3 + 4 * 0) + 1], in3[3 * (3 + 4 * 3) + 2], 0};

		p00.copyFrom(p00t);
		p01.copyFrom(p01t);
		p02.copyFrom(p02t);
		p03.copyFrom(p03t);

		p10.copyFrom(p10t);
		p11.copyFrom(p11t);
		p12.copyFrom(p12t);
		p13.copyFrom(p13t);

		p20.copyFrom(p20t);
		p21.copyFrom(p21t);
		p22.copyFrom(p22t);
		p23.copyFrom(p23t);

		p30.copyFrom(p30t);
		p31.copyFrom(p31t);
		p32.copyFrom(p32t);
		p33.copyFrom(p33t);

		
		Allocation aout = Allocation.createSized(rs, Element.U16_4(rs), 1);
		
		ScriptC_etc1compressor script = new ScriptC_etc1compressor(rs);
		
		script.set_p00(p00);
		script.set_p01(p01);
		script.set_p02(p02);
		script.set_p03(p03);
		
		script.set_p10(p10);
		script.set_p11(p11);
		script.set_p12(p12);
		script.set_p13(p13);
		
		script.set_p20(p20);
		script.set_p21(p21);
		script.set_p22(p22);
		script.set_p23(p23);
		
		script.set_p30(p30);
		script.set_p31(p31);
		script.set_p32(p32);
		script.set_p33(p33);
		
		script.set_mask(amask);
		
		script.forEach_root(aout);
		
		

		short[] arrayOut3Temp = new short[4];
		aout.copyTo(arrayOut3Temp);
		
		Allocation aout2 = Allocation.createSized(rs, Element.U8(rs), 8);
		aout2.copyFromUnchecked(arrayOut3Temp);
		
		byte[] arrayOut3 = new byte[8];
		aout2.copyTo(arrayOut3);
		
		System.out.println("end");
	}
}
