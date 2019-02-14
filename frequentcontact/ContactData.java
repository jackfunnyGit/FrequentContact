package com.asus.launcher.search.frequentcontact;

import android.database.Cursor;
import android.provider.ContactsContract;

public class ContactData {
    public static final String[] PROJECTION = {
            ContactsContract.Contacts._ID, // DO NOT CHANGE! Necessary for list view
            ContactsContract.Contacts.LOOKUP_KEY,
            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
            ContactsContract.Contacts.PHOTO_THUMBNAIL_URI,
            ContactsContract.Contacts.HAS_PHONE_NUMBER,
            //with out this,the cursor result returned back will not consistent with the expectation
            //TODO figure out why in the future
            "times_used"
    };

    public interface CONTACT_INDEX {
        int CONTACT_ID_INDEX = 0;
        int LOOKUP_KEY_INDEX = 1;
        int DISPLAY_NAME_INDEX = 2;
        int PHOTO_THUMBNAIL_INDEX = 3;
        int HAS_PHONE_NUMBER_INDEX = 4;
    }

    private final int mContactId;
    private final String mDisplayName;
    private final String mContactKey;
    private final String mPhotoQueryResult;
    private final boolean mHasPhoneNumber;

    //TODO fix in the future
    public boolean backOrientation;
    public boolean state;
    public float position;

    public ContactData(Cursor cursor) {
        mContactId = cursor.getInt(CONTACT_INDEX.CONTACT_ID_INDEX);
        mDisplayName = cursor.getString(CONTACT_INDEX.DISPLAY_NAME_INDEX);
        mContactKey = cursor.getString(CONTACT_INDEX.LOOKUP_KEY_INDEX);
        mPhotoQueryResult = cursor.getString(CONTACT_INDEX.PHOTO_THUMBNAIL_INDEX);
        mHasPhoneNumber = cursor.getInt(CONTACT_INDEX.HAS_PHONE_NUMBER_INDEX) != 0;
    }

    public int getContactId() {
        return mContactId;
    }

    public String getDisplayName() {
        return mDisplayName;
    }

    public String getContactKey() {
        return mContactKey;
    }

    public String getPhotoQueryResult() {
        return mPhotoQueryResult;
    }

    public boolean getHasPhoneNumber() {
        return mHasPhoneNumber;
    }

}
