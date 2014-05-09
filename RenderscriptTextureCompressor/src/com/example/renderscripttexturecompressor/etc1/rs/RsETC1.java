package com.example.renderscripttexturecompressor.etc1.rs;

import java.nio.ByteBuffer;

import com.example.renderscripttexturecompressor.etc1.rs.ScriptC_etc1compressor;

import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;

public class RsETC1 {
	// Copyright 2009 Google Inc.
	// 2011 Nicolas CASTEL
	//
	// Licensed under the Apache License, Version 2.0 (the "License");
	// you may not use this file except in compliance with the License.
	// You may obtain a copy of the License at
	//
	// http://www.apache.org/licenses/LICENSE-2.0
	//
	// Unless required by applicable law or agreed to in writing, software
	// distributed under the License is distributed on an "AS IS" BASIS,
	// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	// See the License for the specific language governing permissions and
	// limitations under the License.

	/**
	 * Size in bytes of an encoded block.
	 */
	public static final int ENCODED_BLOCK_SIZE = 8;

	/**
	 * Size in bytes of a decoded block.
	 */
	public static final int DECODED_BLOCK_SIZE = 48;

	/**
	 * Size of a PKM file header, in bytes.
	 */
	public static final int ETC_PKM_HEADER_SIZE = 16;

	/**
	 * Accepted by the internalformat parameter of glCompressedTexImage2D.
	 */
	public static final int ETC1_RGB8_OES = 0x8D64;

	short etc1_byte;
	int etc1_bool;
	/* unsigned */long etc1_uint32;



	static short clamp(long x) {
		return (short) (x >= 0 ? (x < 255 ? x : 255) : 0);
	}

	static short convert4To8(int b) {
		int c = b & 0xf;
		return (short) ((c << 4) | c);
	}

	static short convert4To8(long b) {
		long c = b & 0xf;
		return (short) ((c << 4) | c);
	}

	static short convert5To8(int b) {
		int c = b & 0x1f;
		return (short) ((c << 3) | (c >> 2));
	}

	static short convert5To8(long b) {
		long c = b & 0x1f;
		return (short) ((c << 3) | (c >> 2));
	}

	static short convert6To8(int b) {
		int c = b & 0x3f;
		return (short) ((c << 2) | (c >> 4));
	}

	static short convert6To8(long b) {
		long c = b & 0x3f;
		return (short) ((c << 2) | (c >> 4));
	}

	static int divideBy255(int d) {
		return (d + 128 + (d >> 8)) >> 8;
	}

	static int convert8To4(int b) {
		int c = b & 0xff;
		return divideBy255(b * 15);
	}

	static int convert8To5(int b) {
		int c = b & 0xff;
		return divideBy255(b * 31);
	}

	/**
	 * Return the size of the encoded image data (does not include size of PKM
	 * header).
	 */
	public static int getEncodedDataSize(int width, int height) {
		return (((width + 3) & ~3) * ((height + 3) & ~3)) >> 1;
	}
	
	private static Allocation p00; // uchar3
	private static Allocation p01; // uchar3
	private static Allocation p02; // uchar3
	private static Allocation p03; // uchar3

	private static Allocation p10; // uchar3
	private static Allocation p11; // uchar3
	private static Allocation p12; // uchar3
	private static Allocation p13; // uchar3

	private static Allocation p20; // uchar3
	private static Allocation p21; // uchar3
	private static Allocation p22; // uchar3
	private static Allocation p23; // uchar3

	private static Allocation p30; // uchar3
	private static Allocation p31; // uchar3
	private static Allocation p32; // uchar3
	private static Allocation p33; // uchar3
	private static Allocation amask; // uchar3
	private static Allocation aout; // uchar3
	
	//  R, G, B. Byte (3 * (x + 4 * y) is the R value of pixel (x, y)
	private static byte[] p00t;
	private static byte[] p01t;
	private static byte[] p02t;
	private static byte[] p03t;

	private static byte[] p10t;
	private static byte[] p11t;
	private static byte[] p12t;
	private static byte[] p13t;

	private static byte[] p20t;
	private static byte[] p21t;
	private static byte[] p22t;
	private static byte[] p23t;

	private static byte[] p30t;
	private static byte[] p31t;
	private static byte[] p32t;
	private static byte[] p33t;

	private static int [] inmask;
	/**
	 * Encode an entire image. pIn - pointer to the image data. Formatted such
	 * that the Red component of pixel (x,y) is at pIn + pixelSize * x + stride
	 * * y + redOffset; pOut - pointer to encoded data. Must be large enough to
	 * store entire encoded image.
	 * @param script 
	 */
	public static int encodeImage(RenderScript rs, ScriptC_etc1compressor script, ByteBuffer pIn, int width, int height,
			int pixelSize, int stride, ByteBuffer compressedImage) {
		if (pixelSize < 2 || pixelSize > 3) {
			return -1;
		}
		block_number = 0;
		final int kYMask[] = { 0x0, 0xf, 0xff, 0xfff, 0xffff };
		final int kXMask[] = { 0x0, 0x1111, 0x3333, 0x7777, 0xffff };
		byte[] block = new byte[DECODED_BLOCK_SIZE];

		// TODO check the ~3
		int encodedWidth = (width + 3) & ~3;
		int encodedHeight = (height + 3) & ~3;

		// int iOut = 0;
		
		int size = width * height / (DECODED_BLOCK_SIZE / 3);

		p00 = Allocation.createSized(rs, Element.U8_3(rs), size); // uchar3
		p01 = Allocation.createSized(rs, Element.U8_3(rs), size); // uchar3
		p02 = Allocation.createSized(rs, Element.U8_3(rs), size); // uchar3
		p03 = Allocation.createSized(rs, Element.U8_3(rs), size); // uchar3

		p10 = Allocation.createSized(rs, Element.U8_3(rs), size); // uchar3
		p11 = Allocation.createSized(rs, Element.U8_3(rs), size); // uchar3
		p12 = Allocation.createSized(rs, Element.U8_3(rs), size); // uchar3
		p13 = Allocation.createSized(rs, Element.U8_3(rs), size); // uchar3

		p20 = Allocation.createSized(rs, Element.U8_3(rs), size); // uchar3
		p21 = Allocation.createSized(rs, Element.U8_3(rs), size); // uchar3
		p22 = Allocation.createSized(rs, Element.U8_3(rs), size); // uchar3
		p23 = Allocation.createSized(rs, Element.U8_3(rs), size); // uchar3

		p30 = Allocation.createSized(rs, Element.U8_3(rs), size); // uchar3
		p31 = Allocation.createSized(rs, Element.U8_3(rs), size); // uchar3
		p32 = Allocation.createSized(rs, Element.U8_3(rs), size); // uchar3
		p33 = Allocation.createSized(rs, Element.U8_3(rs), size); // uchar3
		amask = Allocation.createSized(rs, Element.U32(rs), size);
		aout = Allocation.createSized(rs, Element.U16_4(rs), size);
		
		p00t = new byte[4*size];
		p01t = new byte[4*size];
		p02t = new byte[4*size];
		p03t = new byte[4*size];

		p10t = new byte[4*size];
		p11t = new byte[4*size];
		p12t = new byte[4*size];
		p13t = new byte[4*size];

		p20t = new byte[4*size];
		p21t = new byte[4*size];
		p22t = new byte[4*size];
		p23t = new byte[4*size];

		p30t = new byte[4*size];
		p31t = new byte[4*size];
		p32t = new byte[4*size];
		p33t = new byte[4*size];

		inmask = new int [size];
		
		
		for (int y = 0; y < encodedHeight; y += 4) {
			int yEnd = height - y;
			if (yEnd > 4) {
				yEnd = 4;
			}
			int ymask = kYMask[yEnd];
			for (int x = 0; x < encodedWidth; x += 4) {
				int xEnd = width - x;
				if (xEnd > 4) {
					xEnd = 4;
				}
				int mask = ymask & kXMask[xEnd];
				for (int cy = 0; cy < yEnd; cy++) {
					int q = (cy * 4) * 3;
					int p = pixelSize * x + stride * (y + cy);
					if (pixelSize == 3) {
						for (int cx = 0; cx < xEnd; cx++) {
							int pixel = ((pIn.get(p+2) & 0xFF) << 16) |((pIn.get(p+1) & 0xFF) << 8) | (pIn.get(p) & 0xFF);
							block[q++] = (byte) ((pixel >> 16) & 0xFF);
							block[q++] = (byte) ((pixel >> 8) & 0xFF);
							block[q++] = (byte) (pixel & 0xFF);
							p += pixelSize;
						}
						// pIn.position(p);
						// pIn.get(block, q, xEnd * 3);
						// System.arraycopy(pIn, p, block, q, xEnd * 3);
					} else {
	                    for (int cx = 0; cx < xEnd; cx++) {
	                    	int p1 = pIn.get(p+1) & 0xFF;
	                    	int p2 = pIn.get(p) & 0xFF;
	                    	int pixel = (p1 << 8) | p2;
	                        block[q++] = (byte) convert5To8(pixel >>> 11);
	                        block[q++] = (byte) convert6To8(pixel >>> 5);
	                        block[q++] = (byte) convert5To8(pixel);
	                        p += pixelSize;
	                    }
	                }
				}
				addToInputAllocation(block, mask);
				// System.arraycopy(encoded, 0, compressedImage, iOut,
				// encoded.length);
				// iOut += encoded.length;
			}
		}
		
		fillAllocation();
		
		setAllocation(script);
		script.forEach_root(aout);
		
		short[] arrayOut3Temp = new short[4*size];
		aout.copyTo(arrayOut3Temp);
		
		Allocation aout2 = Allocation.createSized(rs, Element.U8(rs), 8*size);
		aout2.copyFromUnchecked(arrayOut3Temp);
		
		byte[] encoded = new byte[8*size];
		aout2.copyTo(encoded);		
		compressedImage.put(encoded);
		
		compressedImage.position(0);
		return 0;
	}

	private static void setAllocation(ScriptC_etc1compressor script) {
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
	}

	private static int block_number = 0;
	private static void addToInputAllocation(byte[] block, int mask) {
		// TODO Auto-generated method stub
		inmask[block_number] = mask;
		
		//  R, G, B. Byte (3 * (x + 4 * y) is the R value of pixel (x, y)
		byte[] p00t_temp = {block[3 * (0 + 4 * 0)], block[3 * (0 + 4 * 0) + 1], block[3 * (0 + 4 * 0) + 2], 0};
		byte[] p01t_temp = {block[3 * (0 + 4 * 1)], block[3 * (0 + 4 * 1) + 1], block[3 * (0 + 4 * 1) + 2], 0};
		byte[] p02t_temp = {block[3 * (0 + 4 * 2)], block[3 * (0 + 4 * 2) + 1], block[3 * (0 + 4 * 2) + 2], 0};
		byte[] p03t_temp = {block[3 * (0 + 4 * 3)], block[3 * (0 + 4 * 3) + 1], block[3 * (0 + 4 * 2) + 2], 0};

		byte[] p10t_temp = {block[3 * (1 + 4 * 0)], block[3 * (1 + 4 * 0) + 1], block[3 * (1 + 4 * 0) + 2], 0};
		byte[] p11t_temp = {block[3 * (1 + 4 * 1)], block[3 * (1 + 4 * 1) + 1], block[3 * (1 + 4 * 1) + 2], 0};
		byte[] p12t_temp = {block[3 * (1 + 4 * 2)], block[3 * (1 + 4 * 2) + 1], block[3 * (1 + 4 * 2) + 2], 0};
		byte[] p13t_temp = {block[3 * (1 + 4 * 3)], block[3 * (1 + 4 * 3) + 1], block[3 * (1 + 4 * 3) + 2], 0};

		byte[] p20t_temp = {block[3 * (2 + 4 * 0)], block[3 * (2 + 4 * 0) + 1], block[3 * (2 + 4 * 0) + 2], 0};
		byte[] p21t_temp = {block[3 * (2 + 4 * 1)], block[3 * (2 + 4 * 1) + 1], block[3 * (2 + 4 * 1) + 2], 0};
		byte[] p22t_temp = {block[3 * (2 + 4 * 2)], block[3 * (2 + 4 * 2) + 1], block[3 * (2 + 4 * 2) + 2], 0};
		byte[] p23t_temp = {block[3 * (2 + 4 * 3)], block[3 * (2 + 4 * 3) + 1], block[3 * (2 + 4 * 3) + 2], 0};

		byte[] p30t_temp = {block[3 * (3 + 4 * 0)], block[3 * (3 + 4 * 0) + 1], block[3 * (3 + 4 * 0) + 2], 0};
		byte[] p31t_temp = {block[3 * (3 + 4 * 1)], block[3 * (3 + 4 * 0) + 1], block[3 * (3 + 4 * 1) + 2], 0};
		byte[] p32t_temp = {block[3 * (3 + 4 * 2)], block[3 * (3 + 4 * 0) + 1], block[3 * (3 + 4 * 2) + 2], 0};
		byte[] p33t_temp = {block[3 * (3 + 4 * 3)], block[3 * (3 + 4 * 0) + 1], block[3 * (3 + 4 * 3) + 2], 0};
		
		System.arraycopy(p00t_temp, 0, p00t, block_number * 4, 3);
		System.arraycopy(p01t_temp, 0, p01t, block_number * 4, 3);
		System.arraycopy(p02t_temp, 0, p02t, block_number * 4, 3);
		System.arraycopy(p03t_temp, 0, p03t, block_number * 4, 3);
		
		System.arraycopy(p10t_temp, 0, p10t, block_number * 4, 3);
		System.arraycopy(p11t_temp, 0, p11t, block_number * 4, 3);
		System.arraycopy(p12t_temp, 0, p12t, block_number * 4, 3);
		System.arraycopy(p13t_temp, 0, p13t, block_number * 4, 3);
		
		System.arraycopy(p20t_temp, 0, p20t, block_number * 4, 3);
		System.arraycopy(p21t_temp, 0, p21t, block_number * 4, 3);
		System.arraycopy(p22t_temp, 0, p22t, block_number * 4, 3);
		System.arraycopy(p23t_temp, 0, p23t, block_number * 4, 3);

		System.arraycopy(p30t_temp, 0, p30t, block_number * 4, 3);
		System.arraycopy(p31t_temp, 0, p31t, block_number * 4, 3);
		System.arraycopy(p32t_temp, 0, p32t, block_number * 4, 3);
		System.arraycopy(p33t_temp, 0, p33t, block_number * 4, 3);
	}
	
	private static void fillAllocation() {

		amask.copyFrom(inmask);
		
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
	}

	static final byte kMagic[] = { 'P', 'K', 'M', ' ', '1', '0' };

	static final int ETC1_PKM_FORMAT_OFFSET = 6;
	static final int ETC1_PKM_ENCODED_WIDTH_OFFSET = 8;
	static final int ETC1_PKM_ENCODED_HEIGHT_OFFSET = 10;
	static final int ETC1_PKM_WIDTH_OFFSET = 12;
	static final int ETC1_PKM_HEIGHT_OFFSET = 14;

	static final int ETC1_RGB_NO_MIPMAPS = 0;

	static void writeBEUint16(ByteBuffer header, int iOut, int data) {
		header.position(iOut);
		header.put((byte) (data >> 8));
		header.put((byte) data);
	}

	static int readBEUint16(ByteBuffer headerBuffer, int iIn) {
		return (headerBuffer.get(iIn) << 8) | headerBuffer.get(iIn + 1);
	}

	// Format a PKM header

	public static void formatHeader(ByteBuffer header, int width, int height) {
		header.put(kMagic);
		int encodedWidth = (width + 3) & ~3;
		int encodedHeight = (height + 3) & ~3;
		writeBEUint16(header, ETC1_PKM_FORMAT_OFFSET, ETC1_RGB_NO_MIPMAPS);
		writeBEUint16(header, ETC1_PKM_ENCODED_WIDTH_OFFSET, encodedWidth);
		writeBEUint16(header, ETC1_PKM_ENCODED_HEIGHT_OFFSET, encodedHeight);
		writeBEUint16(header, ETC1_PKM_WIDTH_OFFSET, width);
		writeBEUint16(header, ETC1_PKM_HEIGHT_OFFSET, height);
	}

	// Check if a PKM header is correctly formatted.

	public static boolean isValid(ByteBuffer headerBuffer) {
		if (memcmp(headerBuffer, kMagic, kMagic.length)) {
			return false;
		}
		int format = readBEUint16(headerBuffer, ETC1_PKM_FORMAT_OFFSET);
		int encodedWidth = readBEUint16(headerBuffer,
				ETC1_PKM_ENCODED_WIDTH_OFFSET);
		int encodedHeight = readBEUint16(headerBuffer,
				ETC1_PKM_ENCODED_HEIGHT_OFFSET);
		int width = readBEUint16(headerBuffer, ETC1_PKM_WIDTH_OFFSET);
		int height = readBEUint16(headerBuffer, ETC1_PKM_HEIGHT_OFFSET);
		return format == ETC1_RGB_NO_MIPMAPS && encodedWidth >= width
				&& encodedWidth - width < 4 && encodedHeight >= height
				&& encodedHeight - height < 4;
	}

	static boolean memcmp(ByteBuffer headerBuffer, byte[] b, int lenght) {
		for (int i = 0; i < lenght; i++) {
			if (headerBuffer.get(i) != b[i]) {
				return true;
			}
		}
		return false;
	}

	// Read the image width from a PKM header

	public static int getWidth(ByteBuffer pHeader) {
		return readBEUint16(pHeader, ETC1_PKM_WIDTH_OFFSET);
	}

	// Read the image height from a PKM header

	public static int getHeight(ByteBuffer pHeader) {
		return readBEUint16(pHeader, ETC1_PKM_HEIGHT_OFFSET);
	}

}
