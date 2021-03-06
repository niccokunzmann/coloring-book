package org.androidsoft.coloring.util.images;

import android.os.Parcel;
import android.os.Parcelable;

import org.androidsoft.coloring.ui.widget.LoadImageProgress;
import org.androidsoft.coloring.util.imports.BlackAndWhiteImageImport;
import org.androidsoft.coloring.util.imports.ImagePreview;
import org.androidsoft.coloring.util.imports.UriImageImport;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class UrlImageWithPreview extends UrlImage {

    private final List<ThumbNailImage> thumbs = new ArrayList<>();


    public UrlImageWithPreview(URL url, String id, Date lastModified, RetrievalOptions retrievalOptions) {
        super(url, id, lastModified, retrievalOptions);
    }

    @Override
    public void asPreviewImage(ImagePreview preview, LoadImageProgress progress) {
        if (this.thumbs.size() == 0) {
            new UriImageImport(getUri(), progress, preview).startWith(getCache());
            return;
        }
        List<ThumbNailImage> thumbs = new ArrayList(this.thumbs);
        final int width = preview.getWidth();
        Collections.sort(thumbs, new Comparator<ThumbNailImage>() {
            @Override
            public int compare(ThumbNailImage thumb1, ThumbNailImage thumb2) {
                int distance1 = Math.abs(thumb1.getWidth() - width);
                int distance2 = Math.abs(thumb2.getWidth() - width);
                return distance1 - distance2;
            }
        });
        for (ThumbNailImage bestThumb : thumbs) {
            if (bestThumb.canBeRetrieved() || bestThumb.isCached()) {
                bestThumb.asPreviewImage(preview, progress);
                return;
            }
        }
        new UriImageImport(getUri(), progress, preview).startWith(getCache());
    }

    @Override
    public boolean canBePainted() {
        return true;
    }

    @Override
    public void asPaintableImage(ImagePreview preview, LoadImageProgress progress) {
        new BlackAndWhiteImageImport(getUri(), progress, preview).startWith(getCache());
    }


    public void addPreviewImage(ThumbNailImage thumb) {
        thumbs.add(thumb);
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeInt(thumbs.size());
        for (ThumbNailImage thumb : thumbs) {
            parcel.writeParcelable(thumb, i);
        }
    }

    public static Creator CREATOR = new Creator() {
        @Override
        public ImageDB.Image createFromParcel(Parcel parcel) {
            String urlString = parcel.readString();
            String id = parcel.readString();
            Date lastModified = new Date(parcel.readLong());
            Parcelable retrievalOptions = parcel.readParcelable(RetrievalOptions.class.getClassLoader());
            int size = parcel.readInt();
            URL url = null;
            try {
                url = new URL(urlString);
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return new NullImage();
            }
            UrlImageWithPreview image = new UrlImageWithPreview(url, id, lastModified, (RetrievalOptions) retrievalOptions);
            for (int i = 0; i < size; i++) {
                ThumbNailImage thumb = parcel.readParcelable(ThumbNailImage.class.getClassLoader());
                image.addPreviewImage(thumb);
            }
            return image;
        }

        @Override
        public ImageDB.Image[] newArray(int i) {
            return new ImageDB.Image[0];
        }
    };
}
