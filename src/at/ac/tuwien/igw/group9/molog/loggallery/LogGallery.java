package at.ac.tuwien.igw.group9.molog.loggallery;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.Toast;
import at.ac.tuwien.igw.group9.molog.R;

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
            public void onItemClick(AdapterView parent, View v, int position, long id) {
                Toast.makeText(LogGallery.this, "" + position, Toast.LENGTH_SHORT).show();
            }
        });

        // We also want to show context menu for longpressed items in the
        // gallery
        registerForContextMenu(g);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        menu.add(R.string.gallery_2_text);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        Toast.makeText(this, "Longpress: " + info.position, Toast.LENGTH_SHORT).show();
        return true;
    }

    public class ImageAdapter extends BaseAdapter {
        int mGalleryItemBackground;

        public ImageAdapter(Context c) {
            mContext = c;
            // See res/values/attrs.xml for the <declare-styleable> that defines
            // Gallery1.
            TypedArray a = obtainStyledAttributes(R.styleable.Gallery1);
            mGalleryItemBackground = a.getResourceId(R.styleable.Gallery1_android_galleryItemBackground, 0);
            a.recycle();
        }

        public int getCount() {
            return mImageIds.length;
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView i = new ImageView(mContext);

            // i.setImageURI(Uri.fromFile(LogGallery.this.getFileStreamPath("molog.jpg")));
            // try {
            // i.setImageBitmap(BitmapFactory.decodeStream(LogGallery.this.openFileInput("molog.jpg")));
            // } catch (FileNotFoundException e) {
            // // TODO Auto-generated catch block
            // e.printStackTrace();
            // }

            // ---

            i.setImageBitmap(decodeFile(LogGallery.this.getFileStreamPath("molog.jpg")));

            // ---

            // i.setImageResource(mImageIds[position]);
            i.setScaleType(ImageView.ScaleType.FIT_XY);
            i.setLayoutParams(new Gallery.LayoutParams(136, 88));

            // The preferred Gallery item background
            i.setBackgroundResource(mGalleryItemBackground);

            return i;
        }

        private Context mContext;

        private Integer[] mImageIds = { R.drawable.gallery_photo_1, R.drawable.gallery_photo_2,
                R.drawable.gallery_photo_3, R.drawable.gallery_photo_4, R.drawable.gallery_photo_5,
                R.drawable.gallery_photo_6, R.drawable.gallery_photo_7, R.drawable.gallery_photo_8 };

        private Bitmap decodeFile(File f) {
            try {
                // Decode image size
                BitmapFactory.Options o = new BitmapFactory.Options();
                o.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(new FileInputStream(f), null, o);

                // The new size we want to scale to
                final int REQUIRED_SIZE = 70;

                // Find the correct scale value. It should be the power of 2.
                int scale = 1;
                while (o.outWidth / scale / 2 >= REQUIRED_SIZE && o.outHeight / scale / 2 >= REQUIRED_SIZE)
                    scale *= 2;

                // Decode with inSampleSize
                BitmapFactory.Options o2 = new BitmapFactory.Options();
                o2.inSampleSize = scale;
                return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
            } catch (FileNotFoundException e) {
            }
            return null;
        }
    }

}
