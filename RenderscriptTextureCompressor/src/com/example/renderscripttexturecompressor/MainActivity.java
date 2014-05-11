package com.example.renderscripttexturecompressor;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.opengl.ETC1;
import android.opengl.ETC1Util.ETC1Texture;
import android.os.Bundle;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.RenderScript.ContextType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.renderscripttexturecompressor.bench.etc1.ETC1Benchmarck;
import com.example.renderscripttexturecompressor.etc1.rs.ScriptC_etc1compressor;

public class MainActivity extends Activity {

	private TextView mBenchmarkResult;
	private RenderScript mRS;
	private ScriptC_etc1compressor script;
    private ImageView mDisplayView;
    private Bitmap mBitmapOut;

	public void benchmark(View v) {
		long tJava = java.lang.System.currentTimeMillis();
		for(int i = 0; i<10; i++) {
			ETC1Benchmarck.testJavaETC1BlockCompressor();
		}		
		tJava = java.lang.System.currentTimeMillis() - tJava;
		
		long tSdk = java.lang.System.currentTimeMillis();
		for(int i = 0; i<10; i++) {
			ETC1Benchmarck.testSDKETC1BlockCompressor();
		}		
		tSdk = java.lang.System.currentTimeMillis() - tSdk;
		
		ETC1Benchmarck.initBuffer();
		
		long tRsImg = java.lang.System.currentTimeMillis();
		for(int i = 0; i<10; i++) {
			ETC1Texture texture = ETC1Benchmarck.testRsETC1ImageCompressor(mRS, script);		
		}		
		tRsImg = java.lang.System.currentTimeMillis() - tRsImg;
		
		long tJavaImg = java.lang.System.currentTimeMillis();
		for(int i = 0; i<10; i++) {
			ETC1Texture texture1 = ETC1Benchmarck.testJavaETC1ImageCompressor();	
		}		
		tJavaImg = java.lang.System.currentTimeMillis() - tJavaImg;
		
		long tSdkImg = java.lang.System.currentTimeMillis();
		for(int i = 0; i<10; i++) {
			ETC1Texture texture2 = ETC1Benchmarck.testSDKETC1ImageCompressor();
		}		
		tSdkImg = java.lang.System.currentTimeMillis() - tSdkImg;
		
		mBenchmarkResult.setText("Result: \n"
				+ "1 Block 10*t : Rs N/A ms " + "Java " + tJava / 10.0 + " ms \n" + "SDK " + tSdk / 10.0 + " ms\n"
				+ "Image 256*128 : Rs " + tRsImg / 10.0 + " ms " + "Java " + tJavaImg / 10.0 +" ms \n" + "SDK " + tSdkImg / 10.0 + " ms");

	}
	
	public void display_sdk(View v) {
		showEtc1Texture(ETC1Benchmarck.testSDKETC1ImageCompressor());
	}
	
	public void display_java(View v) {
		showEtc1Texture(ETC1Benchmarck.testJavaETC1ImageCompressor());
	}
	
	public void display_rs(View v) {
		showEtc1Texture(ETC1Benchmarck.testRsETC1ImageCompressor(mRS, script));
	}
	
	public void showEtc1Texture(ETC1Texture texture) {
		int width = texture.getWidth();
        int height = texture.getHeight();
        Buffer data = texture.getData();
		
		int pixelSize = 2;
        int stride = pixelSize * width;
        ByteBuffer decodedData = ByteBuffer.allocateDirect(stride*height)
            .order(ByteOrder.nativeOrder());
        ETC1.decodeImage((ByteBuffer) data, decodedData, width, height, pixelSize, stride);
        mBitmapOut.copyPixelsFromBuffer(decodedData);
        mDisplayView.invalidate();
	}
	


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mBenchmarkResult = (TextView) findViewById(R.id.benchmarkText);
		mBenchmarkResult.setText("Result: not run");
		
        mBitmapOut = Bitmap.createBitmap(256, 128, Config.RGB_565);
		
	    mDisplayView = (ImageView) findViewById(R.id.display);
        mDisplayView.setImageBitmap(mBitmapOut);
        
        ETC1Benchmarck.initBuffer();

		mRS = RenderScript.create(this, ContextType.NORMAL);
		script = new ScriptC_etc1compressor(mRS);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.main, container,
					false);
			return rootView;
		}
	}

}
