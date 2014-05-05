package com.example.renderscripttexturecompressor.etc1;

import java.nio.ByteBuffer;

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

	/**
	 * Encode an entire image. pIn - pointer to the image data. Formatted such
	 * that the Red component of pixel (x,y) is at pIn + pixelSize * x + stride
	 * * y + redOffset; pOut - pointer to encoded data. Must be large enough to
	 * store entire encoded image.
	 */
	public static int encodeImage(ByteBuffer pIn, int width, int height,
			int pixelSize, int stride, ByteBuffer compressedImage) {
		if (pixelSize < 2 || pixelSize > 3) {
			return -1;
		}
		final long kYMask[] = { 0x0, 0xf, 0xff, 0xfff, 0xffff };
		final long kXMask[] = { 0x0, 0x1111, 0x3333, 0x7777, 0xffff };
		short[] block = new short[DECODED_BLOCK_SIZE];
		byte[] encoded = new byte[ENCODED_BLOCK_SIZE];

		int encodedWidth = (width + 3) & ~3;
		int encodedHeight = (height + 3) & ~3;

		// int iOut = 0;

		for (int y = 0; y < encodedHeight; y += 4) {
			int yEnd = height - y;
			if (yEnd > 4) {
				yEnd = 4;
			}
			long ymask = kYMask[yEnd];
			for (int x = 0; x < encodedWidth; x += 4) {
				int xEnd = width - x;
				if (xEnd > 4) {
					xEnd = 4;
				}
				long mask = ymask & kXMask[xEnd];
				for (int cy = 0; cy < yEnd; cy++) {
					int q = (cy * 4) * 3;
					int p = pixelSize * x + stride * (y + cy);
					if (pixelSize == 3) {
						for (int cx = 0; cx < xEnd; cx++) {
							long pixel = (pIn.get(p + 2) << 16)
									| (pIn.get(p + 1) << 8) | pIn.get(p);
							block[q++] = (short) ((pixel >> 16) & 0xFF);
							block[q++] = (short) ((pixel >> 8) & 0xFF);
							block[q++] = (short) (pixel & 0xFF);
							p += pixelSize;
						}
						// pIn.position(p);
						// pIn.get(block, q, xEnd * 3);
						// System.arraycopy(pIn, p, block, q, xEnd * 3);
					} else {
						for (int cx = 0; cx < xEnd; cx++) {
							short p1 = pIn.get(p + 1);
							short p2 = pIn.get(p);
							long pixel = (p1 << 8) | p2;
							block[q++] = (short) convert5To8(pixel >> 11);
							block[q++] = (short) convert6To8(pixel >> 5);
							block[q++] = (short) convert5To8(pixel);
							p += pixelSize;
						}
					}
				}
				JavaETC1.encodeBlock(block, mask, encoded);
				compressedImage.put(encoded);
				// System.arraycopy(encoded, 0, compressedImage, iOut,
				// encoded.length);
				// iOut += encoded.length;
			}
		}
		compressedImage.position(0);
		return 0;
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
