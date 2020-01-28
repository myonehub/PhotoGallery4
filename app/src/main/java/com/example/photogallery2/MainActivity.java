//Camera app code provided by Tejinder
//Modified by WM. Search for WM for changes

package com.example.photogallery2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.Date;

import java.util.ArrayList; //WM
import java.util.List; //WM

public class MainActivity extends AppCompatActivity {
    static final int REQUEST_IMAGE_CAPTURE = 1;
    public static final int SEARCH_ACTIVITY_REQUEST_CODE = 0;
    //public static final String EXTRA_MESSAGE = "com.example.photogallery2.MESSAGE";
    String mCurrentPhotoPath;

    //Values associated with saving the caption. WM
    String currentFileName = null; //similar to mCurrentPhotoPath, but the filename only. WM
    List captionList = new ArrayList(); //contains all the captions. WM
    List filenameList = new ArrayList(); //contains all the filenames. WM
    // These two lists should always be the same size. WM
    int currentElement = 0; //The element number of the current image. WM

    Date CurrentDate = null;
    List dateList = new ArrayList<Date>();

    private void displayPhoto(String path) {
        ImageView iv = (ImageView) findViewById(R.id.ivGallery);
        iv.setImageBitmap(BitmapFactory.decodeFile(path));
    }

    private List populateGallery(Date minDate, Date maxDate) {         // getting photos from storage on phone, put them in to the photo gallery
        File file = new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath(), "/Android/data/com.example.photogallery2/files/Pictures"); // put in our project name then it should work
        filenameList = new ArrayList();
        File[] fList = file.listFiles();
        if (fList != null) {
            for (File f : file.listFiles()) {
                //filenameList.add(f.getPath()); //Incompatible with my code! WM
                filenameList.add(f.getName());   //Compatible with my code! WM
            }
        }
        return filenameList;
    }//end populateGallery

    @Override
    public void onResume() {
        super.onResume();
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Date minDate = new Date(Long.MIN_VALUE); // show all the photo, from min date to max date
        Date maxDate = new Date(Long.MAX_VALUE);
        filenameList = populateGallery(minDate, maxDate);  // pupolateGallery take all the pictures from minDate to maxDate, put into photoGallery ( array of filenames)
        //WM: on first run of program, filenameList should be empty.
        //On subsequent runs, there may be pictures and I will have added a blank caption file,
        // and filenameList gets populated by populateGallery().

        //See if the caption file exists. If not, create a blank caption file and verify that it was created.
        boolean captionFileExists = false;
        int dummy1 = 0;
        File captionFile = null;
        captionFile = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);//so far have not created the caption file. This is only the document directory
        File[] williamsfList;
        if(captionFile != null)
        {
            williamsfList = captionFile.listFiles();
            if (williamsfList.length == 0) //If caption file does not exist (I am assuming caption file would be the only one here)
            {
                //Create the caption file
                try{
                    captionFile = File.createTempFile("captions", ".txt", captionFile);}
                catch (IOException e) {/*could not create captionFile*/}
                //Verify that the file was created ***************(for testing only)*****************
                File testCaptionFile = null;
                testCaptionFile = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
                File[] williamsTestfList;
                williamsTestfList = testCaptionFile.listFiles();
                if(williamsTestfList.length == 0) {/*could not list files in document directory*/}
                else//found some file(s)
                {
                    String testReadingCaptionFileName = null;
                    testReadingCaptionFileName = williamsTestfList[0].getAbsolutePath();//Assuming there is only one file here!
                    if(testReadingCaptionFileName.contains("captions"))
                    {
                        captionFileExists = true;//found the caption file! All good!
                    }
                }
            }//end create / verify caption file
            else
            {
                captionFileExists = true; //Caption file exists already! Do not create a new one!
                //But do save the filename!
                String fileName = williamsfList[0].getAbsolutePath(); //Assuming there is only one file here
                File file = new File(fileName);
                captionFile = file;
            }
            dummy1 = 3;
        }
        else{/*could not get document directory*/}
        //if there were no errors, captionFileExists should be true by this point!
        dummy1 = 10;

        //Populate the captionList.
        //The caption file should exist by now! If it is the first run, it will be blank - captionList will remain blank.
        if(captionFileExists)
        {
            //A. Create bufferedReader
            BufferedReader bufferedReader = null;
            FileReader fileReader = null;
            String ret = null;
            try {
                fileReader = new FileReader(captionFile);//captionFile should exist by this point
                bufferedReader = new BufferedReader(fileReader);//source: https://www.journaldev.com/709/java-read-file-line-by-line
            } catch (IOException e) { /*could not create bufferedReader*/ e.printStackTrace(); }
            dummy1 = 6;
            //B. Populate captionList
            if(bufferedReader != null)
            {
                try { ret = bufferedReader.readLine(); } catch (IOException e) {/*could not read line*/ e.printStackTrace(); }
                while (ret != null)
                {
                    captionList.add(ret);
                    try { ret = bufferedReader.readLine(); } catch (IOException e) {/*could not read line*/ e.printStackTrace(); }
                }
            }
            //C. Close bufferedReader if it was opened
            if(bufferedReader != null)
                try { bufferedReader.close(); } catch (IOException e) { /*could not close bufferedReader*/ e.printStackTrace(); }
            dummy1++;
        }//end populating captionList

        //************************************************************************************************************************
        //NEW: need to do the same thing for the date list.
        //See if the date file exists. If not, create a blank date file and verify that it was created.
        boolean dateFileExists = false;
        int dummy2 = 0;
        File dateFile = null;
        dateFile = getExternalFilesDir(Environment.DIRECTORY_MUSIC);//so far have not created the caption file.
        File[] williamsfList2;
        if(dateFile != null)
        {
            williamsfList2 = dateFile.listFiles();
            if (williamsfList2.length == 0) //If caption file does not exist (I am assuming caption file would be the only one here)
            {
                //Create the date file
                try{
                    dateFile = File.createTempFile("dates", ".txt", dateFile);}
                catch (IOException e) {/*could not create dateFile*/}
            }//end create dateFile
            else
            {
                dateFileExists = true; //date file exists already! Do not create a new one!
                //But do save the filename!
                String fileName = williamsfList2[0].getAbsolutePath(); //Assuming there is only one file here
                File file = new File(fileName);
                dateFile = file;
            }
        }
        else{/*could not get "music" directory*/}
        //if there were no errors, dateFileExists should be true by this point!
        dummy1 = 10;

        //Populate the dateList.
        //The date file should exist by now! If it is the first run, it will be blank - dateList will remain blank.
        if(dateFileExists)
        {
            //A. Create bufferedReader
            BufferedReader bufferedReader = null;
            FileReader fileReader = null;
            String ret = null;
            try {
                fileReader = new FileReader(dateFile);//dateFile should exist by this point
                bufferedReader = new BufferedReader(fileReader);//source: https://www.journaldev.com/709/java-read-file-line-by-line
            } catch (IOException e) { /*could not create bufferedReader*/ e.printStackTrace(); }
            dummy1 = 6;
            //B. Populate dateList
            if(bufferedReader != null)
            {
                try { ret = bufferedReader.readLine(); } catch (IOException e) {/*could not read line*/ e.printStackTrace(); }
                while (ret != null)
                {
                    dateList.add(ret);
                    try { ret = bufferedReader.readLine(); } catch (IOException e) {/*could not read line*/ e.printStackTrace(); }
                }
            }
            //C. Close bufferedReader if it was opened
            if(bufferedReader != null)
                try { bufferedReader.close(); } catch (IOException e) { /*could not close bufferedReader*/ e.printStackTrace(); }
        }//end populating dateList
        //*****************************************************************************************************************************

        //  Log.d("onCreate, size", Integer.toString(photoGallery.size()));
        if (filenameList.size() > 0)
        {
            //mCurrentPhotoPath = filenameList.get(currentElement).toString();  //Incompatible with my code! WM
            mCurrentPhotoPath = getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString()+"/"+filenameList.get(currentElement).toString();//Compatible with my code! WM
            //Display the caption for the current image. WM
            TextView textView = (TextView) findViewById(R.id.editTextCaption);
            textView.setText((CharSequence) captionList.get(currentElement));
            //Display the date for the current image.
            TextView textViewforDate = findViewById(R.id.DatetextView);
            textViewforDate.setText((CharSequence) dateList.get(currentElement).toString());
        }
        displayPhoto(mCurrentPhotoPath);
    }//end onCreate

    public void search(View view) {
        Intent intent = new Intent(this, Search.class);
        //EditText editText = (EditText) findViewById(R.id.editText);
        //String message = editText.getText().toString();
        //intent.putExtra(EXTRA_MESSAGE, message);
        startActivityForResult(intent, SEARCH_ACTIVITY_REQUEST_CODE);
    }

    public void takePicture(View v) {   //changed from private to public WM ----------------
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this, "com.example.photogallery2.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }//end takePicture

    public File createImageFile() throws IOException { //changed to public WM -------------------------------
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg",storageDir);
        mCurrentPhotoPath = image.getAbsolutePath();
        Log.d("createImageFile", mCurrentPhotoPath);
        currentFileName = image.getName(); //Added WM to get the filename
        CurrentDate = new Date(image.lastModified());
        return image;
    }//end createImageFile


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            ImageView mImageView = (ImageView) findViewById(R.id.ivGallery);
            mImageView.setImageBitmap(BitmapFactory.decodeFile(mCurrentPhotoPath));
            //Add the image to the list immediately with the default caption "Enter Caption". WM
            filenameList.add(currentFileName);
            captionList.add("Enter Caption");
            dateList.add(CurrentDate);

            //Need to delete caption file and rewrite the caption list to file.
            ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            //Delete entire caption file (I couldn't see how to delete only the contents)
            //and write entire contents of captionList into the file.
            int dummy = 1;
            //1. Find the caption file.
            File storageDir = null;
            storageDir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
            File[] storageDirFlist;
            if(storageDir != null) {
                storageDirFlist = storageDir.listFiles();
                String fileName = storageDirFlist[0].getAbsolutePath(); //Assuming there is only one file here
                File file = new File(fileName);
                //2. Delete the caption file.
                file.delete();
                //2B. Verify that the caption file was deleted (for testing only)
                storageDirFlist = storageDir.listFiles();
                storageDirFlist = storageDir.listFiles();
                //3. Create a new caption file.
                File captionFile = null;
                if (storageDirFlist.length == 0)//It should be 0, I just deleted the file
                {
                    //Create the caption file
                    captionFile = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
                    try{
                        captionFile = File.createTempFile("captions", ".txt", captionFile);}
                    catch (IOException e) {/*could not create captionFile*/}
                    //Verify that the file was created ***************(for testing only)*****************
                    File testCaptionFile = null;
                    testCaptionFile = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
                    File[] williamsTestfList;
                    williamsTestfList = testCaptionFile.listFiles();
                    if(williamsTestfList.length == 0) {/*could not list files in document directory*/}
                    else//found some file(s)
                    {
                        String testReadingCaptionFileName = null;
                        testReadingCaptionFileName = williamsTestfList[0].getAbsolutePath();//Assuming there is only one file here!
                        if(testReadingCaptionFileName.contains("captions"))
                        {
                            //found the caption file all good

                        }
                    }
                }//end create / verify caption file
                else{ //the file wasn't deleted or there were multiple files there???? This should never happen
                }
                //4. Write the entire contents of captionList to the file.
                FileWriter fileWriter = null;
                BufferedWriter bufferedWriter = null;
                int captionListSize = captionList.size();
                try {
                    //trying to make the fileWriter and bufferedWriter outside the for loop
                    fileWriter = new FileWriter(captionFile/*, true*/);
                    //public FileWriter(File file, boolean append)
                    //Constructs a FileWriter object given a File object. If the second argument is true, then bytes
                    //will be written to the end of the file rather than the beginning.
                    bufferedWriter = new BufferedWriter(fileWriter);//source: https://www.baeldung.com/java-write-to-file
                    for (int i = 0; i < captionListSize; i++)
                    {
                        String stringToWrite = captionList.get(i).toString() + "\n";
                        bufferedWriter.write(stringToWrite);
                    }
                }
                catch (IOException e) {e.printStackTrace();}
                if(bufferedWriter != null)
                {
                    try{
                        bufferedWriter.close();
                    }
                    catch (IOException e) {e.printStackTrace();}
                }
                dummy = 2;
                //Verify some of the contents of the file (for testing only)
                BufferedReader bufferedReader = null;
                FileReader fileReader = null;
                String stringFromFile1 = null;
                String stringFromFile2 = null;
                String stringFromFile3 = null;
                try {
                    fileReader = new FileReader(captionFile);//giving it the file object hope this works
                    bufferedReader = new BufferedReader(fileReader);//source: https://www.journaldev.com/709/java-read-file-line-by-line
                    stringFromFile1 = bufferedReader.readLine();
                    stringFromFile2 = bufferedReader.readLine();
                    stringFromFile3 = bufferedReader.readLine();
                    bufferedReader.close();
                } catch (IOException e) {
                    //could not read from test file
                    e.printStackTrace();
                }
                dummy = 6;
            }//end if storageDir != null
            //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

            //*********************************************************************************************************************
            //NEW: Need to do the same for the date file
            //Need to delete date file and rewrite the date list to file.
            //Delete entire date file (I couldn't see how to delete only the contents)
            //and write entire contents of dateList into the file.
            int dummy3 = 1;
            //1. Find the date file.
            File storageDir2 = null;
            storageDir2 = getExternalFilesDir(Environment.DIRECTORY_MUSIC);
            File[] storageDirFlist2;
            if(storageDir2 != null) {
                storageDirFlist2 = storageDir2.listFiles();
                String fileName = storageDirFlist2[0].getAbsolutePath(); //Assuming there is only one file here
                File file = new File(fileName);
                //2. Delete the date file.
                file.delete();
                //3. Create a new date file.
                File dateFile = null;
                storageDirFlist2 = storageDir2.listFiles();
                if (storageDirFlist2.length == 0)//It should be 0, I just deleted the file
                {
                    //Create the date file
                    dateFile = getExternalFilesDir(Environment.DIRECTORY_MUSIC);
                    try{
                        dateFile = File.createTempFile("dates", ".txt", dateFile);}
                    catch (IOException e) {/*could not create captionFile*/}
                }//end create datefile
                else{ //the file wasn't deleted or there were multiple files there???? This should never happen
                }
                //4. Write the entire contents of dateList to the file.
                FileWriter fileWriter = null;
                BufferedWriter bufferedWriter = null;
                int dateListSize = dateList.size();
                try {
                    //trying to make the fileWriter and bufferedWriter outside the for loop
                    fileWriter = new FileWriter(dateFile);
                    bufferedWriter = new BufferedWriter(fileWriter);//source: https://www.baeldung.com/java-write-to-file
                    for (int i = 0; i < dateListSize; i++)
                    {
                        String stringToWrite = dateList.get(i).toString() + "\n";
                        bufferedWriter.write(stringToWrite);
                    }
                }
                catch (IOException e) {e.printStackTrace();}
                if(bufferedWriter != null)
                {
                    try{
                        bufferedWriter.close();
                    }
                    catch (IOException e) {e.printStackTrace();}
                }
                dummy3 = 2;
                //Verify some of the contents of the file (for testing only)
                BufferedReader bufferedReader = null;
                FileReader fileReader = null;
                String stringFromFile1 = null;
                String stringFromFile2 = null;
                String stringFromFile3 = null;
                try {
                    fileReader = new FileReader(dateFile);//giving it the file object hope this works
                    bufferedReader = new BufferedReader(fileReader);//source: https://www.journaldev.com/709/java-read-file-line-by-line
                    stringFromFile1 = bufferedReader.readLine();
                    stringFromFile2 = bufferedReader.readLine();
                    stringFromFile3 = bufferedReader.readLine();
                    bufferedReader.close();
                } catch (IOException e) {
                    //could not read from test file
                    e.printStackTrace();
                }
                dummy = 6;
            }//end if storageDir != null
            //*********************************************************************************************************************

            //Change the current element to the new image. WM
            currentElement = filenameList.size() - 1;
            //Display the caption for the current image. WM
            TextView textView = (TextView) findViewById(R.id.editTextCaption);
            textView.setText((CharSequence) captionList.get(currentElement));

            TextView textViewforDate = findViewById(R.id.DatetextView);
            textViewforDate.setText((CharSequence) dateList.get(currentElement).toString());

        }//end if result OK

        if (requestCode == SEARCH_ACTIVITY_REQUEST_CODE){
            if (resultCode == RESULT_OK)
            {
                File file = new File(Environment.getExternalStorageDirectory()
                        .getAbsolutePath(), "/Android/data/com.example.photogallery2/files/Pictures");
                String get_caption = data.getStringExtra("CAPTION");
                File[] fList = file.listFiles();
                int cap_index = 0;
                filenameList.clear();

                //============================================================================
                //=== need to regenerate the caption list from the caption file. Otherwise fileList and captionList
                //=== have unequal numbers of elements the second search
                //=== (because captionList gets cleared)
                //============================================================================
                //write entire contents of caption file into caption list
                //1. Find the caption file.
                File storageDir = null;
                storageDir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
                File[] storageDirFlist;
                if(storageDir != null) {
                    storageDirFlist = storageDir.listFiles();
                    String fileName = storageDirFlist[0].getAbsolutePath(); //Assuming there is only one file here
                    //2. Write the entire contents of caption file into caption list
                    int captionListSize = captionList.size();//not sure if needed here
                    //A. Create bufferedReader
                    BufferedReader bufferedReader = null;
                    FileReader fileReader = null;
                    String ret = null;
                    try {
                        fileReader = new FileReader(fileName);//captionFile should exist by this point
                        bufferedReader = new BufferedReader(fileReader);//source: https://www.journaldev.com/709/java-read-file-line-by-line
                    } catch (IOException e) { /*could not create bufferedReader*/ e.printStackTrace(); }
                    //B. Populate captionList from file
                    captionList.clear();
                    if(bufferedReader != null)
                    {
                        try { ret = bufferedReader.readLine(); } catch (IOException e) {/*could not read line*/ e.printStackTrace(); }
                        while (ret != null)
                        {
                            captionList.add(ret);
                            try { ret = bufferedReader.readLine(); } catch (IOException e) {/*could not read line*/ e.printStackTrace(); }
                        }
                    }
                    //C. Close bufferedReader if it was opened
                    if(bufferedReader != null)
                        try { bufferedReader.close(); } catch (IOException e) { /*could not close bufferedReader*/ e.printStackTrace(); }
                }//end if storageDir != null
                //=======================================================================================
                //=======================================================================================

                //******************************************************************************************************
                //*** need to regenerate the date list from the date file. Otherwise fileList and captionList and dateList
                //*** have unequal numbers of elements the second search
                //*** (because dateList gets cleared)
                //*********************************************************************************************************
                //write entire contents of date file into date list
                //1. Find the date file.
                File storageDir2 = null;
                storageDir2 = getExternalFilesDir(Environment.DIRECTORY_MUSIC);
                File[] storageDirFlist2;
                if(storageDir2 != null) {
                    storageDirFlist2 = storageDir2.listFiles();
                    String fileName = storageDirFlist2[0].getAbsolutePath(); //Assuming there is only one file here
                    //2. Write the entire contents of caption file into caption list
                    int dateListSize = dateList.size();//not sure if needed here
                    //A. Create bufferedReader
                    BufferedReader bufferedReader = null;
                    FileReader fileReader = null;
                    String ret = null;
                    try {
                        fileReader = new FileReader(fileName);//captionFile should exist by this point
                        bufferedReader = new BufferedReader(fileReader);//source: https://www.journaldev.com/709/java-read-file-line-by-line
                    } catch (IOException e) { /*could not create bufferedReader*/ e.printStackTrace(); }
                    //B. Populate dateList from file
                    dateList.clear();
                    if(bufferedReader != null)
                    {
                        try { ret = bufferedReader.readLine(); } catch (IOException e) {/*could not read line*/ e.printStackTrace(); }
                        while (ret != null)
                        {
                            dateList.add(ret);
                            try { ret = bufferedReader.readLine(); } catch (IOException e) {/*could not read line*/ e.printStackTrace(); }
                        }
                    }
                    //C. Close bufferedReader if it was opened
                    if(bufferedReader != null)
                        try { bufferedReader.close(); } catch (IOException e) { /*could not close bufferedReader*/ e.printStackTrace(); }
                }//end if storageDir != null
                //*****************************************************************************************************
                //*****************************************************************************************************

                List filter_captionList = new ArrayList();
                List filter_dateList = new ArrayList<Date>();
                for (File f : file.listFiles()) {
                    String str = captionList.get(cap_index).toString();
                    if (str.contains(get_caption)) {
                        filenameList.add(f.getName());
                        filter_captionList.add(captionList.get(cap_index).toString());
                        filter_dateList.add(dateList.get(cap_index).toString());
                    }
                    cap_index++;
                }
                captionList.clear();
                captionList = filter_captionList;
                dateList.clear();
                dateList = filter_dateList;
                currentElement = 0;
                ImageView mImageView = (ImageView) findViewById(R.id.ivGallery);
                TextView textView = (TextView) findViewById(R.id.editTextCaption);
                TextView textViewforDate = findViewById(R.id.DatetextView);

                //If the search is cleared, re-enable the snap button
                if(get_caption.isEmpty())
                {
                    Button button = findViewById(R.id.btnSnap);
                    button.setClickable(true);
                }
                //Otherwise disable the snap button. To prevent crashes
                else
                {
                    Button button = findViewById(R.id.btnSnap);
                    button.setClickable(false);
                }

                if(captionList.size() != 0)
                {
                    //Don't do any of these if the search returned nothing!
                    mCurrentPhotoPath = getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/" + filenameList.get(currentElement).toString();
                    currentFileName = filenameList.get(currentElement).toString();
                    mImageView.setImageBitmap(BitmapFactory.decodeFile(mCurrentPhotoPath));
                    textView.setText((CharSequence) captionList.get(currentElement));
                    textViewforDate.setText((CharSequence) dateList.get(currentElement).toString());
                }
                else
                {
                    mCurrentPhotoPath = null;//This should never get used while the search returns nothing!
                    currentFileName = null;//This should never get used while the search returns nothing!
                    mImageView.setImageDrawable(null);//clear the imageview!
                    textView.setText("no files found");
                    textViewforDate.setText("---");
                }

                int test_i = 0;
            }
        }
    }//end onActivityResult


    //Saves the caption. WM
    public void saveCaption(View view){

        int j; //for testing only.

        //Algorithm 3 for saving captions:
        //1. See if there is an image in the imageView.
        //   I will do this indirectly by seeing if currentFileName is not null.
        //2. If not, do nothing.
        //3. If yes, change the caption for the image.

        //Algorithm 3
        if(currentFileName != null)
        {
            //int elementNumber = filenameList.indexOf(currentFileName);
            TextView textView = (TextView) findViewById(R.id.editTextCaption);
            String caption = textView.getText().toString();
            captionList.set(currentElement, caption);

            //Need to delete caption file and rewrite the caption list to file.
            ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            //Delete entire caption file (I couldn't see how to delete only the contents)
            //and write entire contents of captionList into the file.
            int dummy = 1;
            //1. Find the caption file.
            File storageDir = null;
            storageDir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
            File[] storageDirFlist;
            if(storageDir != null) {
                storageDirFlist = storageDir.listFiles();
                String fileName = storageDirFlist[0].getAbsolutePath(); //Assuming there is only one file here
                File file = new File(fileName);
                //2. Delete the caption file.
                file.delete();
                //2B. Verify that the caption file was deleted (for testing only)
                storageDirFlist = storageDir.listFiles();
                storageDirFlist = storageDir.listFiles();
                //3. Create a new caption file.
                File captionFile = null;
                if (storageDirFlist.length == 0)//It should be 0, I just deleted the file
                {
                    //Create the caption file
                    captionFile = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
                    try{
                        captionFile = File.createTempFile("captions", ".txt", captionFile);}
                    catch (IOException e) {/*could not create captionFile*/}
                    //Verify that the file was created ***************(for testing only)*****************
                    File testCaptionFile = null;
                    testCaptionFile = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
                    File[] williamsTestfList;
                    williamsTestfList = testCaptionFile.listFiles();
                    if(williamsTestfList.length == 0) {/*could not list files in document directory*/}
                    else//found some file(s)
                    {
                        String testReadingCaptionFileName = null;
                        testReadingCaptionFileName = williamsTestfList[0].getAbsolutePath();//Assuming there is only one file here!
                        if(testReadingCaptionFileName.contains("captions"))
                        {
                            //found the caption file all good

                        }
                    }
                }//end create / verify caption file
                else{ //the file wasn't deleted or there were multiple files there???? This should never happen
                }
                //4. Write the entire contents of captionList to the file.
                FileWriter fileWriter = null;
                BufferedWriter bufferedWriter = null;
                int captionListSize = captionList.size();
                try {
                    //trying to make the fileWriter and bufferedWriter outside the for loop
                    fileWriter = new FileWriter(captionFile/*, true*/);
                    //public FileWriter(File file, boolean append)
                    //Constructs a FileWriter object given a File object. If the second argument is true, then bytes
                    //will be written to the end of the file rather than the beginning.
                    bufferedWriter = new BufferedWriter(fileWriter);//source: https://www.baeldung.com/java-write-to-file
                    for (int i = 0; i < captionListSize; i++)
                    {
                        String stringToWrite = captionList.get(i).toString() + "\n";
                        bufferedWriter.write(stringToWrite);
                    }
                }
                catch (IOException e) {e.printStackTrace();}
                if(bufferedWriter != null)
                {
                    try{
                        bufferedWriter.close();
                    }
                    catch (IOException e) {e.printStackTrace();}
                }
                dummy = 2;
                //Verify some of the contents of the file (for testing only)
                BufferedReader bufferedReader = null;
                FileReader fileReader = null;
                String stringFromFile1 = null;
                String stringFromFile2 = null;
                String stringFromFile3 = null;
                try {
                    fileReader = new FileReader(captionFile);//giving it the file object hope this works
                    bufferedReader = new BufferedReader(fileReader);//source: https://www.journaldev.com/709/java-read-file-line-by-line
                    stringFromFile1 = bufferedReader.readLine();
                    stringFromFile2 = bufferedReader.readLine();
                    stringFromFile3 = bufferedReader.readLine();
                    bufferedReader.close();
                } catch (IOException e) {
                    //could not read from test file
                    e.printStackTrace();
                }
                dummy = 6;
            }//end if storageDir != null
            //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        }

        j = 1;
    }//end method

    //Move to the newer image. WM
    public void Left(View view)
    {
        //Other commands that might be useful
        //textView.onCommitCompletion();

        //Algorithm:
        //1. See if the number of images is greater than 1.
        //2. If not, do nothing.
        //3. If yes, continue with the next steps.
        //4. See if the current image is an older image.
        //   I will do this by checking the currentElement.
        //5. If not, do nothing.
        //6. If yes, continue with the next steps.
        //7. Save the caption.
        //8. Set the current image to the newer image.

        int j; //for testing only.

        //See if the number of images is greater than 1.
        int filenameListSize = filenameList.size();
        if(filenameListSize > 1)
        {
            //See if the current image is an older image.
            //The current image is an older image if currentElement is not the last element number.
            if(currentElement != (filenameListSize - 1))
            {
                //Algorithm 3 for saving captions:
                //1. See if there is an image in the imageView.
                //   I will do this indirectly by seeing if currentFileName is not null.
                //2. If not, do nothing.
                //3. If yes, change the caption for the image.

                j = 1;

                //Get a handle to the newer image
                String newerImageName = filenameList.get(currentElement + 1).toString();
                String temporaryCopy = newerImageName;
                File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                newerImageName = storageDir + "/" + newerImageName;
                j = 5;
                //Set the current image to the newer image.
                mCurrentPhotoPath = newerImageName;
                currentFileName = temporaryCopy;
                currentElement++;
                //Draw the current image.
                ImageView mImageView = (ImageView) findViewById(R.id.ivGallery);
                mImageView.setImageBitmap(BitmapFactory.decodeFile(mCurrentPhotoPath));
                //Display the caption for the current image.
                TextView textView = (TextView) findViewById(R.id.editTextCaption);
                textView.setText((CharSequence) captionList.get(currentElement));


                TextView textViewforDate = findViewById(R.id.DatetextView);
                textViewforDate.setText((CharSequence) dateList.get(currentElement).toString());
            }
        }
        j = 1;
    }//end method

    //Display the older image. WM
    public void Right(View view)
    {
        //Algorithm:
        //1. See if the number of images is greater than 1.
        //2. If not, do nothing.
        //3. If yes, continue with the next steps.
        //4. See if the current image is a newer image.
        //   I will do this by checking the currentElement.
        //5. If not, do nothing.
        //6. If yes, continue with the next steps.
        //7. Set the current image to the more recently taken image.

        int j = 1; //for testing only.

        //See if the number of images is greater than 1.
        int filenameListSize = filenameList.size();
        if(filenameListSize > 1)
        {
            //See if the current image is a newer image.
            //The current image is a newer image if currentElement is not the first element number.
            if(currentElement != 0)
            {
                //Get a handle to the older image
                String olderImageName = filenameList.get(currentElement - 1).toString();
                String temporaryCopy = olderImageName;
                File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                olderImageName = storageDir + "/" + olderImageName;
                j = 5;
                //Set the current image to the older image.
                mCurrentPhotoPath = olderImageName;
                currentFileName = temporaryCopy;
                currentElement--;
                //Draw the current image.
                ImageView mImageView = (ImageView) findViewById(R.id.ivGallery);
                mImageView.setImageBitmap(BitmapFactory.decodeFile(mCurrentPhotoPath));
                //Display the caption for the current image.
                TextView textView = (TextView) findViewById(R.id.editTextCaption);
                textView.setText((CharSequence) captionList.get(currentElement));


                TextView textViewforDate = findViewById(R.id.DatetextView);
                textViewforDate.setText((CharSequence) dateList.get(currentElement).toString());
            }
        }
        j = 2;
    }//end method
}

