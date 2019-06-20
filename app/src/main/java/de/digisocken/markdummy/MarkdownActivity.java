package de.digisocken.markdummy;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import us.feras.mdv.MarkdownView;

public class MarkdownActivity extends AppCompatActivity {
	public static String PACKAGE_NAME;

	public static final int WRITE_PERMISSION_REQ = 42;
    public static final int FILEREQCODE = 1234;
	public static final String NOTE_FILENAME = "/notes.md";

	private EditText markdownEditText;
	private MarkdownView markdownView;
	private Uri data = null;

	private int viewmode = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.markdown_view);
		markdownEditText = (EditText) findViewById(R.id.markdownText);
		markdownView = (MarkdownView) findViewById(R.id.markdownView);
		PACKAGE_NAME = getApplicationContext().getPackageName();
		String text = "";

		Intent intent = getIntent();
		data = intent.getData();
		if (data == null) {
			File file = null;
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
				file = new File(
						Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
						PACKAGE_NAME
				);
			} else {
				file = new File(Environment.getExternalStorageDirectory() + "/Documents/"+PACKAGE_NAME);
			}

			String path = file.getPath() + NOTE_FILENAME;
			data = Uri.fromFile(new File(path));
		}

		try {
			InputStream input = getContentResolver().openInputStream(data);
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(input));

			text = "";
			while (bufferedReader.ready()) {
				text += bufferedReader.readLine() + "\n";
			}
			markdownEditText.setText(text);
		} catch (Exception e) {
			e.printStackTrace();
		}

		updateMarkdownView();

		markdownEditText.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable s) {}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				updateMarkdownView();
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.pref, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.saveAction:
				clickSaveButton(null);
				return true;

			case R.id.viewAction:
				int viewmode_t = (viewmode + 1) % 3;
                viewmode = 0;
                updateMarkdownView();
                viewmode = viewmode_t;
				if (viewmode == 0) {
                    markdownEditText.setVisibility(View.VISIBLE);
                    markdownView.setVisibility(View.VISIBLE);
                } else if (viewmode == 1) {
                    markdownEditText.setVisibility(View.GONE);
                    markdownView.setVisibility(View.VISIBLE);
                } else if (viewmode == 2) {
                    markdownEditText.setVisibility(View.VISIBLE);
                    markdownView.setVisibility(View.GONE);
                }
				return true;

			case R.id.openAction:
                Intent intentFileChooser = new Intent()
                        .setType(Intent.normalizeMimeType("*/*"))
                        .setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intentFileChooser, "open"), FILEREQCODE);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void clickSaveButton(View v) {
		if (PermissionUtils.writeGranted(this)) {
			saveNow();
		} else {
			String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
			PermissionUtils.requestPermissions(this, WRITE_PERMISSION_REQ, permissions);
		}
	}

	void saveNow() {
		if (data != null) {
			File file = null;
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
				file = new File(
						Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
						PACKAGE_NAME
				);
			} else {
				file = new File(Environment.getExternalStorageDirectory() + "/Documents/"+PACKAGE_NAME);
			}

			String path = file.getPath() + NOTE_FILENAME;
			try {
				if (!file.exists()) {
					Log.d(PACKAGE_NAME, "on save mkdirs()");
					file.mkdirs();
				}
				file = new File(path);
				if (!file.exists()) file.createNewFile();
			} catch (Exception e) {
				Toast.makeText(
						getApplicationContext(),
						"Mist\n" + data.getPath(),
						Toast.LENGTH_LONG
				).show();
			}

			path = data.getPath();
			if (path != null) {
				try {
					Log.d(PACKAGE_NAME, "saveNow()");

					path = PathUtil.getPath(getApplicationContext(), data);
					file = new File(path);
					FileOutputStream fos = new FileOutputStream(file);
					fos.write(markdownEditText.getText().toString().getBytes());
					fos.flush();
					fos.close();

					Toast.makeText(
							getApplicationContext(),
							"Ok\n" + data.getPath(),
							Toast.LENGTH_SHORT
					).show();

				} catch (Exception e) {

					e.printStackTrace();
					Toast.makeText(
							getApplicationContext(),
							"Mist !!!\n" + path,
							Toast.LENGTH_LONG
					).show();
				}
			}
		}
	}

	private void updateMarkdownView() {
	    if (viewmode == 0) {
            markdownView.loadMarkdown(
                    markdownEditText.getText().toString(),
                    "file:///android_asset/blue.css"
            );
        }
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		boolean granted = false;
		switch (requestCode) {
			case WRITE_PERMISSION_REQ:
				granted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
				if (granted) {
					saveNow();
				} else {
					//nobody knows what to do
				}
				break;
			default:
				super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		}
	}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if(requestCode == FILEREQCODE && resultCode == RESULT_OK) {

            if (intent != null) {
                data = intent.getData();

                Log.d(PACKAGE_NAME, "intent.getData() is not Null - Use App via filemanager?");
                try {
                    InputStream input = getContentResolver().openInputStream(data);
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(input));

                    String text = "";
                    while (bufferedReader.ready()) {
                        text += bufferedReader.readLine() + "\n";
                    }
                    markdownEditText.setText(text);
                    int viewmode_t = viewmode;
                    viewmode = 0;
                    updateMarkdownView();
                    viewmode = viewmode_t;

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        final TextView textBox = (TextView) findViewById(R.id.markdownText);
        CharSequence userText = textBox.getText();
        outState.putCharSequence("savedText", userText);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        final TextView textBox = (TextView) findViewById(R.id.markdownText);
        CharSequence userText = savedInstanceState.getCharSequence("savedText");
        textBox.setText(userText);
        int viewmode_t = viewmode;
        viewmode = 0;
        updateMarkdownView();
        viewmode = viewmode_t;
    }
}
