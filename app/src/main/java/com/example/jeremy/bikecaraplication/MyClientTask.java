package com.example.jeremy.bikecaraplication;

/**
 * Created by Jeremy on 3/5/2016.
 */
import android.os.AsyncTask;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
public class MyClientTask extends AsyncTask<Void, Void, Void>{


    String dstAddress;
    int dstPort;
    String response;
    Socket socket;
    Boolean isNewData = false;
    String data;

    MyClientTask(String addr, int port){
        dstAddress = addr;
        dstPort = port;
    }
    public void close() throws IOException {
        socket.close();
    }
    @Override
    protected Void doInBackground(Void... arg0) {
        try {
            socket = new Socket(dstAddress, dstPort);
            InputStream inputStream = socket.getInputStream();



    while(true) {
        byte[] buffer = new byte[1024];

        int read = inputStream.read(buffer);
        String response1 = new String(buffer, 0, read);
        isNewData = true;
        data = response1;
    }
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    void sendDataToRPi(String toSend) {
        try {
            if(toSend != "") {
                OutputStream output = socket.getOutputStream();
                byte[] b = toSend.getBytes(Charset.forName("UTF-8"));
                output.write(b);
            }

        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
   @Override
    protected void onPostExecute(Void result) {
        //MainActivity.server_Response.setText(response);
       super.onPostExecute(result);
    }

}
