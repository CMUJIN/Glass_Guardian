package com.jinhs.helper;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.util.Log;

public class FileReadingHelper {
	@SuppressWarnings("resource")
	public static byte[] convertToByteArray(String fileName){
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(fileName);
		} catch (FileNotFoundException e1) {
			Log.d("file reading failed",e1.getMessage());
			return null;
		}
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        try {
            for (int readNum; (readNum = fis.read(buf)) != -1;) {
                bos.write(buf, 0, readNum); //no doubt here is 0
                //Writes len bytes from the specified byte array starting at offset off to this byte array output stream.
                System.out.println("read " + readNum + " bytes,");
            }
        } catch (IOException e) {
            Log.d("file convert failed", e.getMessage());
        }
        return bos.toByteArray();
	}
}
