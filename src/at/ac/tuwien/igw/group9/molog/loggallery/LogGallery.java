package at.ac.tuwien.igw.group9.molog.loggallery;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.Toast;
import at.ac.tuwien.igw.group9.molog.R;
import at.ac.tuwien.igw.group9.molog.db.LogData;

public class LogGallery extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.loggallery);

		// Reference the Gallery view
		Gallery g = (Gallery) findViewById(R.id.gallery);
		// Set the adapter to our custom adapter (below)
		g.setAdapter(new ImageAdapter(this));

		// Set a item click listener, and just Toast the clicked position
		g.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView parent, View v, int position,
					long id) {
				LogData logData = new LogData(LogGallery.this);
				Cursor cursor = logData.all();
				cursor.moveToPosition(position);
				SimpleDateFormat sdf = new SimpleDateFormat();
				Toast.makeText(LogGallery.this, "Captured at " + sdf.format(new Date(cursor.getLong(1))), Toast.LENGTH_SHORT).show();
			}
		});

		// We also want to show context menu for longpressed items in the
		// gallery
		registerForContextMenu(g);
	}

//	@Override
//	public void onCreateContextMenu(ContextMenu menu, View v,
//			ContextMenuInfo menuInfo) {
//		menu.add(R.string.gallery_2_text);
//	}
//
//	@Override
//	public boolean onContextItemSelected(MenuItem item) {
//		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
//				.getMenuInfo();
//		Toast.makeText(this, "Longpress: " + info.position, Toast.LENGTH_SHORT)
//				.show();
//		return true;
//	}

	public class ImageAdapter extends BaseAdapter {
		int mGalleryItemBackground;
		LogData mLogData;
		Cursor mCursor;

		public ImageAdapter(Context c) {
			mContext = c;
			mLogData = new LogData(mContext);
			mCursor = mLogData.all();
			// See res/values/attrs.xml for the <declare-styleable> that defines
			// Gallery1.
			// TypedArray a = obtainStyledAttributes(R.styleable.Gallery1);
			// mGalleryItemBackground =
			// a.getResourceId(R.styleable.Gallery1_android_galleryItemBackground,
			// 0);
			// a.recycle();
		}

		public int getCount() {
			return mLogData.count();
		}

		public Object getItem(int position) {
			return position;
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView imageView = new ImageView(mContext);

			// i.setImageURI(Uri.fromFile(LogGallery.this.getFileStreamPath("molog.jpg")));
			// try {
			// i.setImageBitmap(BitmapFactory.decodeStream(LogGallery.this.openFileInput("molog.jpg")));
			// } catch (FileNotFoundException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }

			// imageView.setImageBitmap(decodeFile(LogGallery.this
			// .getFileStreamPath("molog.jpg")));

			mCursor.moveToPosition(position);
			imageView.setImageBitmap(decodeFile(LogGallery.this
					.getFileStreamPath(mCursor.getString(4))));

			// imageView.setImageResource(mImageIds[position]);
			imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
			// i.setLayoutParams(new Gallery.LayoutParams(136, 88));

			imageView.setLayoutParams(new Gallery.LayoutParams(
					WindowManager.LayoutParams.MATCH_PARENT,
					WindowManager.LayoutParams.MATCH_PARENT));
			// The preferred Gallery item background
			// imageView.setBackgroundResource(mGalleryItemBackground);
			imageView.setBackgroundColor(Color.BLACK);

			return imageView;
		}

		private Context mContext;

		// private Integer[] mImageIds = { R.drawable.gallery_photo_1,
		// R.drawable.gallery_photo_2,
		// R.drawable.gallery_photo_3, R.drawable.gallery_photo_4,
		// R.drawable.gallery_photo_5,
		// R.drawable.gallery_photo_6, R.drawable.gallery_photo_7,
		// R.drawable.gallery_photo_8 };

		private Bitmap decodeFile(File f) {
			try {
				// Decode image size
				BitmapFactory.Options o = new BitmapFactory.Options();
				o.inJustDecodeBounds = true;
				BitmapFactory.decodeStream(new FileInputStream(f), null, o);

				// The new size we want to scale to
				final int REQUIRED_SIZE = 800;

				// Find the correct scale value. It should be the power of 2.
				int scale = 1;
				while (o.outWidth / scale / 2 >= REQUIRED_SIZE
						&& o.outHeight / scale / 2 >= REQUIRED_SIZE)
					scale *= 2;

				// Decode with inSampleSize
				BitmapFactory.Options o2 = new BitmapFactory.Options();
				o2.inSampleSize = scale;

				// Rotate image right (compensate camera portrait mode)
				Matrix matrix = new Matrix();
				matrix.postRotate(90);
				Bitmap image = BitmapFactory.decodeStream(
						new FileInputStream(f), null, o2);
				Bitmap rotated = Bitmap.createBitmap(image, 0, 0,
						image.getWidth(), image.getHeight(), matrix, false);

				// free base image
				image.recycle();
				image = null;

				return rotated;
			} catch (FileNotFoundException e) {
			}
			return null;
		}
	}

}
