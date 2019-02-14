package com.asus.launcher.search.frequentcontact;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
import android.text.TextUtils;
import android.util.LruCache;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableRow;

import com.asus.launcher.R;
import com.asus.launcher.search.SearchModule;
import com.asus.launcher.search.contact.ContactUtil;
import com.asus.launcher.search.module.ModuleList;
import com.asus.launcher.search.module.ViewPagerModule;
import com.asus.launcher.search.pref.Preferences;
import com.asus.launcher.util.PermissionUtils;
import com.asus.quickfind.util.LogUtilities;
import com.asus.quickfind.util.Utilities;
import com.asus.quickfind.view.pager.GridPagerAdapter;
import com.asus.quickfind.view.pager.IconPagerAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FrequentContactModule extends ViewPagerModule<ViewPagerModule.ViewHolder> {
    private static final String LOG_TAG = "FrequentContactModule";
    private static final int MAX_PAGE_COUNT_NO_KEYWORDS = 1;
    private static final int FREQUENT_CONTACTS_LOADER_ID = 20;
    private Context mContext;
    private String mKeyword;
    private final IconPagerAdapter mPagerAdapter;
    private final ContactIconItem.Listener mIconListener;
    private static final int PHOTO_CACHE_SIZE = 8;
    private LruCache<String, Bitmap> mPhotoCache = new LruCache<>(PHOTO_CACHE_SIZE);
    private ExecutorService mContactTaskExecutor = Executors.newFixedThreadPool(Utilities
            .ExecutorThreadPoolSize.CONTACTS);

    public FrequentContactModule(@NonNull final Activity activity,
                                 final SearchModuleCallback searchModuleCallback) {

        super(ModuleList.ModuleId.FREQUENT_CONTACT,
                SearchModule.SHOW_WHEN_KEYWORD_EMPTY,
                activity, searchModuleCallback);
        mContext = activity;
        Resources res = mContext.getResources();
        mIconListener = new ContactIconItem.Listener();
        int rowNumber = res.getInteger(R.integer.quick_find_frequent_contact_page_row_number);
        int columnNumber = res.getInteger(R.integer.quick_find_frequent_contact_page_column_number);
        mPagerAdapter = new IconPagerAdapter(mContext, rowNumber, columnNumber,
                R.layout.quick_find_app_search_single, R.id.icon, R.id.text);
        initPagerAdapter();
        if (!isModuleEnabled(mContext)) {
            hideModule();
            return;
        }
        initContactLoader();
    }

    @Override
    public ViewHolder createViewHolder(ViewGroup parent) {
        return new ViewHolder(parent, mPagerAdapter);
    }

    @Override
    public void bindViewHolder(ViewHolder holder) {
        holder.toggleViewsVisibility(!mPagerAdapter.isDummyPageShowing());
    }

    @Override
    protected void onSearchKeywordChanged(@NonNull String keyword) {
        mKeyword = keyword;
        if (!(TextUtils.isEmpty(mKeyword))) {
            LogUtilities.i(LOG_TAG, "search keyword is not empty ... return ");
            hideModule();
            return;
        }
        for (ViewHolder vh : getViewHolders()) {
            // we don't show progress bar every time, only the 1st time loading
            vh.getViewPager().setVisibility(View.INVISIBLE);
        }
        showModule();
        restartContactLoader();
    }

    @Override
    public boolean isModuleEnabled(Context context) {
        if (!PermissionUtils.check(mContext, PermissionUtils.FEATURE.QUICK_FIND)) {
            LogUtilities.i(LOG_TAG, "PermissionUtils false ... return ");
            return false;
        }
        return Preferences.isFrequentContactEnabledWhenKeywordEmpty(context);
    }


    private void initPagerAdapter() {
        mPagerAdapter.setMaxPageCount(MAX_PAGE_COUNT_NO_KEYWORDS);
        mPagerAdapter.showDummyPage();
        mPagerAdapter.setIconListener(mIconListener);
        mPagerAdapter.setIconCache(mPhotoCache);
        mPagerAdapter.setExecutor(mContactTaskExecutor);
    }

    private void initContactLoader() {
        getActivity().getLoaderManager().initLoader(FREQUENT_CONTACTS_LOADER_ID, null,
                mContactLoaderCallback);
    }

    private void restartContactLoader() {
        getActivity().getLoaderManager().restartLoader(FREQUENT_CONTACTS_LOADER_ID, null,
                mContactLoaderCallback);
    }

    private final LoaderManager.LoaderCallbacks<Cursor> mContactLoaderCallback =
            new LoaderManager.LoaderCallbacks<Cursor>() {
                @Override
                public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                    Uri frequentContentUri = Uri.withAppendedPath(ContactsContract.Contacts
                            .CONTENT_URI, "frequent");
                    LogUtilities.i(LOG_TAG, "onCreateLoader with Uri = " + frequentContentUri);
                    return new CursorLoader(mContext, frequentContentUri, ContactData
                            .PROJECTION, null, null, null);
                }

                @Override
                public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                    if (!TextUtils.isEmpty(mKeyword)) {
                        hideModule();
                        return;
                    }
                    if (data == null) {
                        LogUtilities.e(LOG_TAG, "cursor data is null,which is not expected!!!");
                        hideModule();
                        return;
                    }
                    final int contactCount = data.getCount();
                    LogUtilities.i(LOG_TAG, "onLoadFinished contactCount = " + contactCount);
                    if (contactCount == 0) {
                        hideModule();
                    } else {
                        mPagerAdapter.setItemList(generateContactItem(data));
                        for (ViewHolder holder : getViewHolders()) {
                            holder.toggleViewsVisibility(true);
                            //TODO delete in the future
                            //setActionView(holder);
                        }
                        showModule();
                    }

                }

                @Override
                public void onLoaderReset(Loader<Cursor> loader) {
                    mPagerAdapter.setItemList(null);
                }
            };

    private List<ContactIconItem> generateContactItem(@NonNull Cursor cursor) {
        final List<ContactIconItem> itemList = new ArrayList<>();
        cursor.moveToPosition(-1);
        while (cursor.moveToNext()) {
            ContactData contactData = new ContactData(cursor);
            ContactIconItem contactIconItem = new ContactIconItem(contactData);
            initContactIconItem(contactIconItem);
            itemList.add(contactIconItem);
        }
        return itemList;
    }

    private void initContactIconItem(ContactIconItem contactIconItem) {
        ContactData contactData = contactIconItem.contactData;
        final String uriPhotoResult = contactData.getPhotoQueryResult();
        if (TextUtils.isEmpty(uriPhotoResult)) {
            return;
        }
        contactIconItem.setLoadListener(new IconPagerAdapter.IconItem.LoadListener() {
            @WorkerThread
            @Override
            public Bitmap loadIconInBackground(@NonNull Context context) {
                return ContactUtil.queryContactPhoto(context, Uri.parse(uriPhotoResult));
            }
        });

    }

    private void setActionView(ViewHolder holder) {
        Object object = holder.getViewPager().getTag();
        if (!(object instanceof GridPagerAdapter.PageViewHolder)) {
            LogUtilities.e(LOG_TAG, "ViewTag is not PageViewHolder!!");
            return;
        }
        GridPagerAdapter.PageViewHolder pageViewHolder = (GridPagerAdapter.PageViewHolder) object;
        //TODO consider two Row
        TableRow tableRow = pageViewHolder.getRow(0);
        tableRow.setTag(pageViewHolder.getGrid(0));
        View actionView = UtilityAnimation.getActionView(mContext, tableRow, R.layout
                .quick_find_action_view);
        UtilityAnimation.setOnclickAnimate(tableRow, actionView);
    }

    @Override
    public void onDestroy() {
        mPagerAdapter.onDestroy();
        ContactIconItem.clearDefaultPhoto();
    }
}
