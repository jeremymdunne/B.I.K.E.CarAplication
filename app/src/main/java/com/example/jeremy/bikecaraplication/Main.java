package com.example.jeremy.bikecaraplication;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.graphics.Matrix;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.util.Log;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.widget.Toast;
import android.graphics.Canvas;
import android.graphics.Point;
import android.view.MotionEvent;
import org.osmdroid.ResourceProxy;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


import android.os.Bundle;

public class Main extends ActionBarActivity implements SensorEventListener, Runnable, OnInitListener {
    public GeoPoint[] demoMockLocations = {
            new GeoPoint(35.917573, -78.597070),
            new GeoPoint(35.917479, -78.596775),
            new GeoPoint(35.917401, -78.596528),
            new GeoPoint(35.917347, -78.596346),
            new GeoPoint(35.917288, -78.596142),
            new GeoPoint(35.917238, -78.595968),
            new GeoPoint(35.917186, -78.595796),
            new GeoPoint(35.917133, -78.595634),
            new GeoPoint(35.917063, -78.595422),
            new GeoPoint(35.916998, -78.595209),
            new GeoPoint(35.916933, -78.595046),
            new GeoPoint(35.916868, -78.594831),
            new GeoPoint(35.916794, -78.594611),
            new GeoPoint(35.916733, -78.594450),
            new GeoPoint(35.916733, -78.594450),
            new GeoPoint(35.916733, -78.594450),
            new GeoPoint(35.916712, -78.594395),
            new GeoPoint(35.916694, -78.594347),
            new GeoPoint(35.916674, -78.594293),
            new GeoPoint(35.916651, -78.594242),
            new GeoPoint(35.916629, -78.594206),
            new GeoPoint(35.916603, -78.594162),
            new GeoPoint(35.916540, -78.594038),
            new GeoPoint(35.916466, -78.593950),
            new GeoPoint(35.916388, -78.593850),
            new GeoPoint(35.916292, -78.593748)

    };

    int iterationToDisplayBikesAtNextIntersection = 40;
    int iterationToFakeConnectAt = 7;
    public GeoPoint[][] possibleBikeLocations = {
            {new GeoPoint(35.916225, -78.594522), new GeoPoint(35.917157, -78.594139)}
    };

    public boolean shouldHardcodeHeading = true;
    public int hardcodedHeading = 270;

    public int demoLocationIteration = 0;
    public int lastRotation;
    ImageView leftArrow, rightArrow;
    public float phone_Heading = -1;
    public SensorManager mSensorManager;
    MyClientTask BIKEServer;
    //public String serverAddress = "192.168.1.5";
    public String serverAddress = "192.168.43.115";
    public int port = 8086;
    public long lastDemoUpdate;
    public long timeBetweenDemoUpdates = 1500;
    public boolean shouldRunDemo = false;
    public Sensor mOrientation;
    public boolean shouldEnd = false;
    public boolean shouldStart = false;
    public boolean shouldSendNewHeading = false;
    public String idStatesment = "ID,CAR";
    private static final String TAG = Main.class.getSimpleName();
    private TextToSpeech myTTS;
    private int MY_DATA_CHECK_CODE = 0;
    ArrayList<OverlayItem> overlayItemArray;
    public long timeLastLeft, timeLastRight = System.currentTimeMillis();
    public long timeBetweenWarning = 4000;
    ArrayList<OverlayItem> anotherOverlayItemArray;
    private MapView myOpenMapView;
    public boolean shouldRestart = false; 
    private MapController myMapController;
    public boolean[] bicycleLocation = {false, false};
    public boolean shouldConnectToServer = true;
    public int numTimesWarnOfBicyclist = 3;
    public int[] numTimesWarned = {0,0};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        leftArrow = (ImageView) findViewById(R.id.leftArrow);
        rightArrow = (ImageView) findViewById(R.id.rightArrow);
        Intent checkTTSIntent = new Intent();
        checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mOrientation = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        //RotationGestureOverlay mRotationGestureOverlay = new RotationGestureOverlay(context, mMapView);
        //mRotationGestureOverlay.setEnabled(true);
        //mMapView.setMultiTouchControls(true);
        //myOpenMapView.getOverlays().add(this.mRotationGestureOverlay);


        Thread thread = new Thread(Main.this);
        thread.setDaemon(false);
        thread.start();

    }

    public void showBicyclistLoc(char where, int whichRoad) {
        if (where == 'L') {
            anotherOverlayItemArray = new ArrayList<OverlayItem>();

            OverlayItem olItem = new OverlayItem("Bicycle", "Bicycle Heading Towards You On Left", possibleBikeLocations[whichRoad][0]);

            Bitmap myBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bike_left);
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(myBitmap, myBitmap.getWidth(), myBitmap.getHeight(), true);
            Matrix matrix = new Matrix();
            matrix.setRotate(0);
            Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
            Drawable newMarker = new BitmapDrawable(getResources(), rotatedBitmap);
            olItem.setMarker(newMarker);
            olItem.setMarkerHotspot(OverlayItem.HotspotPlace.CENTER);
            anotherOverlayItemArray.add(olItem);
            ItemizedIconOverlay<OverlayItem> anotherItemizedIconOverlay = new ItemizedIconOverlay<OverlayItem>(this, anotherOverlayItemArray, null);
            myOpenMapView.getOverlays().add(anotherItemizedIconOverlay);
        }
        if (where == 'R') {
            anotherOverlayItemArray = new ArrayList<OverlayItem>();
            OverlayItem olItem = new OverlayItem("Bicycle", "Bicycle Heading Towards You On Right", possibleBikeLocations[whichRoad][1]);
            Bitmap myBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bike_right);
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(myBitmap, myBitmap.getWidth(), myBitmap.getHeight(), true);
            Matrix matrix = new Matrix();
            matrix.setRotate(0);
            Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
            Drawable newMarker = new BitmapDrawable(getResources(), rotatedBitmap);
            olItem.setMarker(newMarker);
            olItem.setMarkerHotspot(OverlayItem.HotspotPlace.CENTER);
            anotherOverlayItemArray.add(olItem);
            ItemizedIconOverlay<OverlayItem> anotherItemizedIconOverlay = new ItemizedIconOverlay<OverlayItem>(this, anotherOverlayItemArray, null);
            myOpenMapView.getOverlays().add(anotherItemizedIconOverlay);
        }
    }


    private void setOverlayLoc(GeoPoint mockLoc) {
        overlayItemArray.clear();
        OverlayItem newMyLocationItem = new OverlayItem("My Location", "My Location", mockLoc);
        overlayItemArray.add(newMyLocationItem);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub
        if (!shouldRunDemo) {
            shouldRunDemo = true;
        } else {
            shouldRunDemo = false;
        }
        return super.onTouchEvent(event);
    }


    public void startDemo() {
        shouldRunDemo = true;
    }

    public void pauseDemo() {
        shouldRunDemo = false;
    }

    public void updateDemoLocation() {
        GeoPoint demoLoc;
        int roadToDisplay = 0;
        if (demoLocationIteration >= iterationToDisplayBikesAtNextIntersection) roadToDisplay = 1;
        if (shouldRunDemo) {
            if (System.currentTimeMillis() > lastDemoUpdate + timeBetweenDemoUpdates) {
                if (demoLocationIteration + 1 < demoMockLocations.length) {
                    demoLocationIteration++;
                    myOpenMapView.getOverlays().clear();
                    showUser(demoMockLocations[demoLocationIteration]);

                    if (bicycleLocation[0]) showBicyclistLoc('R', roadToDisplay);
                    if (bicycleLocation[1]) showBicyclistLoc('L', roadToDisplay);
                    //myOpenMapView.setRotation(demoRotations[demoLocationIteration]);
                    myMapController.setCenter(demoMockLocations[demoLocationIteration]);
                    lastDemoUpdate = System.currentTimeMillis();
                    int userRotation = getRotation();
                    changeMapOrientation(userRotation - 90);
                }
            } else {
                if (demoLocationIteration + 2 < demoMockLocations.length) {

                    double timeElapsedInStretch = System.currentTimeMillis() - lastDemoUpdate;
                    double timeScale = (timeElapsedInStretch / timeBetweenDemoUpdates);
                    double deltaX = demoMockLocations[demoLocationIteration + 1].getLongitude() - demoMockLocations[demoLocationIteration].getLongitude();
                    double deltaYInStretch = (deltaX * timeScale) + demoMockLocations[demoLocationIteration].getLongitude();
                    double deltaXInStretch = ((demoMockLocations[demoLocationIteration + 1].getLatitude() - demoMockLocations[demoLocationIteration].getLatitude()) * timeScale) + demoMockLocations[demoLocationIteration].getLatitude();
                    //myOpenMapView.getOverlays().clear();
                    myOpenMapView.getOverlays().clear();
                    showUser(new GeoPoint(deltaXInStretch, deltaYInStretch));
                    if (bicycleLocation[0]) showBicyclistLoc('R', roadToDisplay);
                    if (bicycleLocation[1]) showBicyclistLoc('L', roadToDisplay);
                    //myOpenMapView.setRotation(demoRotations[demoLocationIteration]);
                    myMapController.setCenter(new GeoPoint(deltaXInStretch, deltaYInStretch));
                    //lastDemoUpdate = System.currentTimeMillis();
                    int userRotation = getRotation();
                    changeMapOrientation(userRotation - 90);
                }
            }
        }
    }

    public void updateDemoLocationOld() {
        GeoPoint demoLoc;
        if (shouldRunDemo) {
            if (System.currentTimeMillis() > lastDemoUpdate + timeBetweenDemoUpdates) {
                if (demoLocationIteration + 1 < demoMockLocations.length) {
                    demoLocationIteration++;
                    myOpenMapView.getOverlays().clear();
                    showUser(demoMockLocations[demoLocationIteration]);
                    if (bicycleLocation[0]) showBicyclistLoc('R', 0);
                    if (bicycleLocation[1]) showBicyclistLoc('L', 0);
                    //myOpenMapView.setRotation(demoRotations[demoLocationIteration]);
                    myMapController.setCenter(demoMockLocations[demoLocationIteration]);
                    lastDemoUpdate = System.currentTimeMillis();
                    int userRotation = getRotation();
                    changeMapOrientation(userRotation - 90);
                }
            }
        }
    }

    public int getRotation() {
        if (demoLocationIteration + 1 < demoMockLocations.length) {
            int deltaX = (int) ((demoMockLocations[demoLocationIteration + 1].getLongitude() - demoMockLocations[demoLocationIteration].getLongitude()) * 1000000);
            int deltaY = (int) ((demoMockLocations[demoLocationIteration + 1].getLatitude() - demoMockLocations[demoLocationIteration].getLatitude()) * 1000000);
            int touchAngle = (int) Math.toDegrees(Math.atan2(deltaY, deltaX));
            Log.e(TAG, "Rotation Angle:" + Integer.toString(touchAngle));
            lastRotation = touchAngle;
            return touchAngle;
        } else {
            return lastRotation;
        }
    }

    public void showUser(GeoPoint point) {

        anotherOverlayItemArray = new ArrayList<OverlayItem>();
        OverlayItem olItem = new OverlayItem("Here", "SampleDescription", point);
        Bitmap myBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.car);
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(myBitmap, myBitmap.getWidth(), myBitmap.getHeight(), true);
        Matrix matrix = new Matrix();
        matrix.setRotate(0);
        Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
        Drawable newMarker = new BitmapDrawable(getResources(), rotatedBitmap);
        olItem.setMarker(newMarker);
        olItem.setMarkerHotspot(OverlayItem.HotspotPlace.CENTER);
        anotherOverlayItemArray.add(olItem);
        ItemizedIconOverlay<OverlayItem> anotherItemizedIconOverlay = new ItemizedIconOverlay<OverlayItem>(this, anotherOverlayItemArray, null);
        myOpenMapView.getOverlays().add(anotherItemizedIconOverlay);


    }

    private void updateLoc(GeoPoint loc) {
        myMapController.setCenter(loc);
        setOverlayLoc(loc);
    }

    public void onInit(int initStatus) {
        //check for successful instantiation
        if (initStatus == TextToSpeech.SUCCESS) {
            if (myTTS.isLanguageAvailable(Locale.US) == TextToSpeech.LANG_AVAILABLE)
                myTTS.setLanguage(Locale.US);
        } else if (initStatus == TextToSpeech.ERROR) {
            Toast.makeText(this, "Sorry! Text To Speech failed...", Toast.LENGTH_LONG).show();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MY_DATA_CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                //the user has the necessary data - create the TTS
                myTTS = new TextToSpeech(this, this);
            } else {
                //no data - install it now
                Intent installTTSIntent = new Intent();
                installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installTTSIntent);
            }
        }
    }


    private void speakWords(String speech) {
//implement TTS here
        myTTS.speak(speech, TextToSpeech.QUEUE_FLUSH, null);
    }

    public void setVisibility(final ImageView image, final int which) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (which == 0) {
                    image.setVisibility(View.INVISIBLE);
                } else {
                    image.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    public void changeMapOrientation(final int which) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                myOpenMapView.setMapOrientation(which);
            }
        });
    }

    public void showAlert(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(Main.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void startMap() {
        myOpenMapView = (MapView) findViewById(R.id.openmapview);
        myOpenMapView.setTileSource(TileSourceFactory.MAPQUESTOSM);
        myOpenMapView.setBuiltInZoomControls(true);
        myMapController = (MapController) myOpenMapView.getController();
        myMapController.setCenter(demoMockLocations[0]);
        myMapController.setZoom(17);
        myOpenMapView.setUseDataConnection(true);

        myMapController.setCenter(demoMockLocations[0]);
        myOpenMapView.invalidate();
        myMapController.setCenter(demoMockLocations[0]);
        myOpenMapView.setMultiTouchControls(true);

    }

    public void waitForStart(){
        while (!shouldRunDemo) {
            delayForMillis(100);
        }

    }

    public void waitForFakeConnection(){
        while (iterationToFakeConnectAt < demoLocationIteration) {
            updateDemoLocation();
            delayForMillis(500);
        }
    }

    public void demoRun() {

        myOpenMapView.getOverlays().clear();
        waitForStart();
        waitForFakeConnection();
        try {


            while (shouldEnd == false & !shouldRestart) {
                while (!shouldRunDemo) delayForMillis(100);
                if (shouldSendNewHeading) {
                    sendCompassHeading();
                    delayForMillis(1000);
                    shouldSendNewHeading = false;
                }
                updateDemoLocation();
                //userRotation = getRotation();
                //changeMapOrientation(userRotation);
                Log.d(TAG, "Looped");
                delayForMillis(1000);
                if (iterationToFakeConnectAt < demoLocationIteration) {
                    getServerReply();
                }
                if (iterationToFakeConnectAt == demoLocationIteration) {
                    showAlert("Connecting To Intersection");

                }
            }

    if(shouldRestart){
        shouldRestart = false;
        demoRun();
    }

        } catch (Exception e) {

            e.printStackTrace();
            try {
                closeSocket();
                Log.d(TAG, "Ended");
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }




    public void run() {
        startMap();
        int userRotation = getRotation();
        changeMapOrientation(userRotation);
        //myOpenMapView.setRotation(demoRotations[demoLocationIteration]);
            delayForMillis(5000);
            //todo check if rotation successful
         userRotation = getRotation();
        changeMapOrientation(userRotation);

        if(shouldConnectToServer) {
            //delayForMillis(3000);
            BIKEServer = new MyClientTask(serverAddress, port);
            //delayForMillis(3000);
            BIKEServer.execute();
            delayForMillis(1000);
            sendID();
            delayForMillis(300);
            Log.d(TAG, "ID sent");
            sendCompassHeading();
            Log.d(TAG, "Heading Sent");
            //delayForMillis(1000);
            demoRun();
        }
           // closeSocket();
        else

        {
            while (true) {
                while (!shouldRunDemo) delayForMillis(100);
                updateDemoLocation();
                warnOfCyclist('L');
                delayForMillis(500);
                //userRotation = getRotation();

                //myOpenMapView.setMapOrientation(userRotation);

            }
        }
        }

    public void closeSocket() throws Exception{
        try {
            BIKEServer.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    public void sendID(){
        BIKEServer.sendDataToRPi(idStatesment);

    }

    public void connectToServer(){
        shouldStart = true;
    }

    public void sendNewHeading(){
        shouldSendNewHeading = true;

    }

    public void restartDemo(){
        shouldRestart = true;
        demoLocationIteration = 0;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        if(id == R.id.newHeading){
            sendNewHeading();
        }
        if(id == R.id.Connect){
            connectToServer();
        }
        if(id == R.id.pauseDemo){
            pauseDemo();
        }
        if(id == R.id.startDemo){
            startDemo();
        }
        if(id == R.id.restart){
            restartDemo();
        }

        return super.onOptionsItemSelected(item);
    }

    public double getHeading(){
        if(shouldHardcodeHeading){
            return hardcodedHeading;
        }
        else if(phone_Heading != -1){
            return phone_Heading;
        }
        else{
            return -1;
        }
    }

    protected void onResume() {

        super.onResume();
        mSensorManager.registerListener(this, mOrientation, SensorManager.SENSOR_DELAY_NORMAL);


    }
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        phone_Heading = event.values[0];
        //heading.setText(String.valueOf(phone_Heading));
        float pitch_angle = event.values[1];
        float roll_angle = event.values[2];


    }

    public void warnOfCyclist(char which) {
        if (shouldRunDemo) {

            if (which == 'L') {
                if(numTimesWarnOfBicyclist > numTimesWarned[0]){
                    if (System.currentTimeMillis() - timeLastLeft > timeBetweenWarning) {
                        speakWords("Bicycle on Left");
                        timeLastLeft = System.currentTimeMillis();
                        numTimesWarned[0] ++;
                    }

                }
            } else {
                if(numTimesWarnOfBicyclist > numTimesWarned[1]){
                    if (System.currentTimeMillis() - timeLastRight > timeBetweenWarning) {
                        speakWords("Bicycle on Right");
                        timeLastRight = System.currentTimeMillis();
                        numTimesWarned[1] ++;
                    }
                }
            }
        }
    }

    private void sendCompassHeading() {
        double my_heading = phone_Heading;
        if(shouldHardcodeHeading) my_heading = hardcodedHeading;
    while(my_heading == -1){
        delayForMillis(100);
        my_heading = phone_Heading;
    }
        int compHeading = Math.round((int) my_heading);
        String toSend = "Comp," + Integer.toString(compHeading);
        BIKEServer.sendDataToRPi(toSend);
    }

    private void getServerReply() {
        //request info
        String toSend = "RDY";
        BIKEServer.sendDataToRPi(toSend);
        //wait for info
        int timePased = 0;
        while(BIKEServer.isNewData == false){
            delayForMillis(100);
            BIKEServer.sendDataToRPi(toSend);
            Log.v(TAG,"NOTHING REPLIED<LOOPING");
        }
        delayForMillis(10);
        String response = BIKEServer.data;
        BIKEServer.isNewData = false;
        BIKEServer.data = null;
        //String data = getNewData();
        boolean[] bikeLoc = parseForBikeLocations(response);
        if(bikeLoc[1]){
            setVisibility(rightArrow, 1);
            warnOfCyclist('R');
            bicycleLocation[1] = true;
        }
        else{
            setVisibility(rightArrow, 0);
            bicycleLocation[1] = false;
        }
        if(bikeLoc[0]){
            setVisibility(leftArrow, 1);
            warnOfCyclist('L');
            bicycleLocation[0] = true;
        }
        else{
            setVisibility(leftArrow, 0);
            bicycleLocation[0] = false;
        }
    }

    public String getNewData(){
        BIKEServer.sendDataToRPi("RDY");
        while(BIKEServer.isNewData == false){
            delayForMillis(100);
        }
        delayForMillis(10);
        String response = BIKEServer.data;
        BIKEServer.isNewData = false;
        BIKEServer.data = null;
        return response;
    }


    private boolean[] parseForBikeLocations(String data){
        Log.d(TAG, "MESSAGE TO PARSE: " + data);
        boolean[] toReturn = {false, false};
        int rightStart = data.indexOf("R,");
        int leftStart = data.indexOf("L,");
        int leftStatus = Integer.parseInt(data.substring(rightStart + 2, rightStart + 3));
        int rightStatus = Integer.parseInt(data.substring(leftStart + 2, leftStart + 3));
        if(leftStatus == 1){
            toReturn[1] = true;
        }
        if(rightStatus == 1){
            toReturn[0] = true;
        }
        return toReturn;

    }
    public void delayForMillis(int millis){
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }



    @Override
    public void onAccuracyChanged(Sensor sensor,int accuracy){
        // TODO Auto-generated method stub

    }

    private class MyItemizedIconOverlay extends ItemizedIconOverlay<OverlayItem> {

        public MyItemizedIconOverlay(
                List<OverlayItem> pList,
                org.osmdroid.views.overlay.ItemizedIconOverlay.OnItemGestureListener<OverlayItem> pOnItemGestureListener,
                ResourceProxy pResourceProxy) {
            super(pList, pOnItemGestureListener, pResourceProxy);
            // TODO Auto-generated constructor stub
        }

        @Override
        public void draw(Canvas canvas, MapView mapview, boolean arg2) {
            // TODO Auto-generated method stub
            super.draw(canvas, mapview, arg2);

            if(!overlayItemArray.isEmpty()){

                //overlayItemArray have only ONE element only, so I hard code to get(0)
                GeoPoint in = overlayItemArray.get(0).getPoint();

                Point out = new Point();
                mapview.getProjection().toPixels(in, out);
                Paint drawPaint = new Paint();
                drawPaint.setColor(Color.BLUE);
                drawPaint.setAntiAlias(true);

                canvas.drawCircle(
                        out.x - 20/2,  //shift the bitmap center
                        out.y - 20/2,  //shift the bitmap center
                        20,
                        drawPaint);
            }
        }

        @Override
        public boolean onSingleTapUp(MotionEvent event, MapView mapView) {
            // TODO Auto-generated method stub
            //return super.onSingleTapUp(event, mapView);
            return true;
        }
    }


}
