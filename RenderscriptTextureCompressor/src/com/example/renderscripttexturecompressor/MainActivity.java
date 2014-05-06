package com.example.renderscripttexturecompressor;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.RenderScript.ContextType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.renderscripttexturecompressor.bench.etc1.ETC1Benchmarck;
import com.example.renderscripttexturecompressor.etc1.rs.ScriptC_etc1compressor;

public class MainActivity extends Activity {
	
    private TextView mBenchmarkResult;
    private RenderScript mRS;
    private ScriptC_etc1compressor script;
    
	public void benchmark(View v) {
        long t = java.lang.System.currentTimeMillis();
        ETC1Benchmarck.testRsETC1BlockCompressor(mRS, script);
		//ETC1Benchmarck.testETC1ImageCompressor(mRS);
        t = java.lang.System.currentTimeMillis() - t;
        mBenchmarkResult.setText("Result: " + t + " ms");
    }


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

        mBenchmarkResult = (TextView) findViewById(R.id.benchmarkText);
        mBenchmarkResult.setText("Result: not run");
		
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
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
		}
	}

}
