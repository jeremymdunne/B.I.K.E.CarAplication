package com.example.jeremy.bikecaraplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;

/**
 * Created by Jeremy on 3/25/2016.
 */
public class CustomResourceProxy extends DefaultResourceProxyImpl

    {

        private final Context mContext;
        public CustomResourceProxy(Context pContext) {
            super(pContext);
            mContext = pContext;
        }

        @Override
        public Bitmap getBitmap(final ResourceProxy.bitmap pResId) {
            switch (pResId){
                case person:
                    //your image goes here!!!
                    return BitmapFactory.decodeResource(mContext.getResources(), R.drawable.person);
            }
            return super.getBitmap(pResId);
        }

        @Override
        public Drawable getDrawable(final ResourceProxy.bitmap pResId) {
            switch (pResId){
                case person:
                    return mContext.getResources().getDrawable(com.example.jeremy.bikecaraplication.R.drawable.person);
            }
            return super.getDrawable(pResId);
        }
}
