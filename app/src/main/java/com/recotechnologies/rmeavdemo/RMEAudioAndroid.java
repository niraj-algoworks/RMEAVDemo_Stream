// This header may not be removed.  It should accompany all source code
// derived from this code in any form or fashion.
// Filename: RMEAVAudio.java
// Author: Homayoon Beigi <beigi@recotechnologies.com>
// Copyright (c) 2003-2020 Recognition Technologies, Inc.
// Date: October 27, 2020
// This code may be used as is and may not be distributed or copied without the
// explicit knowledge and permission of the author or Recognition Technologies, Inc.

package com.recotechnologies.rmeavdemo;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Process;
import android.util.Log;

import com.recotechnologies.rmeavapi.RMEAudio;
import com.recotechnologies.rmeavapi.RMEAudioEventListener;
import com.recotechnologies.rmeavapi.RMEAudioUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static android.media.AudioFormat.CHANNEL_IN_STEREO;

public class RMEAudioAndroid extends RMEAudio {

  protected RMEAudioEventListener m_sAudioEventListener;
  protected int m_iRecordingInProgress = 0;

  public int m_iSaveLocalAudio = 0;

  //NEW AUDIO
  protected int m_iBufferSize;
  protected int m_iSamplingRate = 16000;
  protected int m_iISCDebug = 0;

  protected static final int MIN_BUFFER_SIZE_MS = 1000;
  protected AudioRecord m_sAudioRecord = null;
  protected Thread m_sAudioThread = null;

   protected static final int CHANNELS = AudioFormat.CHANNEL_IN_STEREO;
  //protected static final int CHANNELS = AudioFormat.CHANNEL_IN_MONO;
  protected static final int ENCODING = AudioFormat.ENCODING_PCM_16BIT;
  //protected static final int SOURCE = MediaRecorder.AudioSource.MIC;
  //protected static final int SOURCE = MediaRecorder.AudioSource.UNPROCESSED;
  //protected static final int SOURCE = MediaRecorder.AudioSource.VOICE_COMMUNICATION;
  protected static final int SOURCE = MediaRecorder.AudioSource.VOICE_RECOGNITION;


  public RMEAudioAndroid(RMEAudioEventListener sAudioEventListener, int iISCDebug) {
    super(sAudioEventListener,iISCDebug);
     this.m_sAudioEventListener=sAudioEventListener;
     this.m_iISCDebug=iISCDebug;
  }

  public void StartAudio(int iSamplingRate, long hAVEngineHandle) {
    m_iSamplingRate = iSamplingRate;

    double minBufferSize = (double) m_iSamplingRate * MIN_BUFFER_SIZE_MS / 1000;

    m_iBufferSize = AudioRecord.getMinBufferSize(m_iSamplingRate, CHANNELS, ENCODING);

    if (m_iBufferSize < minBufferSize)
      m_iBufferSize = m_iBufferSize * (int) Math.ceil(minBufferSize / m_iBufferSize);

    m_iSamplingRate = iSamplingRate;

    if (m_iISCDebug > 1)
      Log.d(m_strClassTag, String.format("m_iBufferSize = %d (%.3f seconds)",
              m_iBufferSize, (double) m_iBufferSize / iSamplingRate));

    //    m_sAudioRecord = new AudioRecord(SOURCE, m_iSamplingRate, CHANNELS, ENCODING, m_iBufferSize);
    m_sAudioRecord = new AudioRecord.Builder()
            .setAudioSource(SOURCE)
            .setAudioFormat(new AudioFormat.Builder()
                    .setEncoding(ENCODING)
                    .setChannelMask(CHANNEL_IN_STEREO)
                    .setSampleRate(m_iSamplingRate)
                    .build())
            .setBufferSizeInBytes(m_iBufferSize)
            .build();

    if (m_sAudioRecord == null || m_sAudioRecord.getState() != AudioRecord.STATE_INITIALIZED)
    {
      Log.e(m_strClassTag, "Could not initialize audio device at " + m_iSamplingRate + " Hz.");
      m_sAudioRecord = null;
    }

    m_iRecordingInProgress = 1;
    if (m_sAudioEventListener==null){
        Log.e(m_strClassTag + "95","audio event listener null");
        return;
    }
    m_sAudioEventListener.SetRecordingInProgress(hAVEngineHandle, m_iRecordingInProgress);
    if (m_iISCDebug > 1)
      Log.d(m_strClassTag, "AudioThread");
    m_sAudioThread = new Thread(new AudioRunnable(hAVEngineHandle), "AudioThread");
    m_sAudioThread.start();
  }


  public void StopAudio(long hAVEngineHandle) {
    if (m_iISCDebug > 1)
      Log.d(m_strClassTag, "RMEAudio::StopAudio: Called");
    if (m_sAudioThread != null && m_sAudioThread.isAlive())
    {
      if (m_iISCDebug > 1)
        Log.d(m_strClassTag, "THREAD NOT NULL AND IS ALIVE");
      if (m_iISCDebug > 1)
        Log.d(m_strClassTag, "Stopping audio thread.");

      m_iRecordingInProgress = 0;
      if (m_sAudioEventListener==null){
        Log.e(m_strClassTag + "118","audio event listener null");
        return;
      }
      m_sAudioEventListener.SetRecordingInProgress(hAVEngineHandle, m_iRecordingInProgress);

      try
      {
        if (m_iISCDebug > 1)
          Log.d(m_strClassTag, "THREAD TRY KILL");
        m_sAudioThread.join();
        m_sAudioThread = null;

        if (m_sAudioRecord != null)
        {
          if (m_iISCDebug > 1)
            Log.d(m_strClassTag, "RMEAudio::StopAudio: Calling m_sAudioRecord.stop()");
          m_sAudioRecord.stop();
          m_sAudioRecord.release();
        }
      }
      catch (InterruptedException e)
      {
        Log.e(m_strClassTag, "" + e);
      }
    }
  }


  protected class AudioRunnable implements Runnable {
    protected long m_hAVEngineHandle = 0;

    public AudioRunnable(long hAVEngineHandle) {
      this.m_hAVEngineHandle = hAVEngineHandle;
    } // AudioRunnable

    public long EngineHandle() {
      return m_hAVEngineHandle;
    }

    @Override
    public void run() {
      if (m_iISCDebug > 1)
        Log.d(m_strClassTag, "AudioRunnable:  Inside run");

      Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);

      try
      {
        m_sAudioRecord.startRecording();
      } catch (IllegalStateException e) {
        Log.e(m_strClassTag, e.toString());
      }

      //      final int BufferElements2Rec_VAD = 480;
      final int BufferElements2Rec_VAD = 4000;
      short sData[] = new short[BufferElements2Rec_VAD];
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      int iCount = 0;
      int iSerialNum=0;

      while (m_iRecordingInProgress == 1) {
        if (m_iISCDebug > 1)
          Log.d("AudioThread::run", "Recording is in Progress");

        iCount++;
        int result = m_sAudioRecord.read(sData, 0, BufferElements2Rec_VAD);
        if (result < 0) {
          throw new RuntimeException("Reading of audio buffer failed: " +
                  getBufferReadFailureReason(result));
        }

        try {
          short[] mCopyData=new short[sData.length/2];
          for (int i=0;i<mCopyData.length;++i){
            mCopyData[i]=sData[i*2];
          }

          byte byData[] = RMEAudioUtils.short2byte(mCopyData);

          if (m_iSaveLocalAudio > 0) {
            // kept incase we want to record audio locally to debug
            //        final File file = new File(Environment.getExternalStorageDirectory(), "recording.pcm");
            final File file = new File("/sdcard/rmeroot/data/rmespch/media", "recording.pcm");
            try (final FileOutputStream outStream = new FileOutputStream(file, true)) {
              if (m_iISCDebug > 1)
                Log.d("AudioThread::run:", "" + byData.length);
              outStream.write(byData);
            }
            catch (IOException e) {
              Log.e(m_strClassTag, ""+e);
            }
          }

          if (m_sAudioEventListener==null){
            Log.e(m_strClassTag + "219","audio event listener null");
            return;
          }
          int iResult = m_sAudioEventListener.AVEnginePushPCMAudioDataByteArray(EngineHandle(),
                  byData,
                  sData.length,
                  iSerialNum,
                  "");
        }
        catch (Exception e) {
          Log.e(m_strClassTag, "" + e);
        }

/*
        if (iCount % 12 == 0)
        {
          m_sAudioEventListener.AVEngineCompletePCMAudio(EngineHandle(), iSerialNum);
          iSerialNum++;
        }
*/
      }

      if (m_sAudioEventListener==null){
        Log.e(m_strClassTag + "236","audio event listener null");
        return;
      }
      m_sAudioEventListener.AVEngineCompletePCMAudio(EngineHandle(), iSerialNum);
      iSerialNum++;
    }

    protected String getBufferReadFailureReason(int errorCode)
    {
      switch (errorCode)
      {
        case AudioRecord.ERROR_INVALID_OPERATION:
          return "ERROR_INVALID_OPERATION";
        case AudioRecord.ERROR_BAD_VALUE:
          return "ERROR_BAD_VALUE";
        case AudioRecord.ERROR_DEAD_OBJECT:
          return "ERROR_DEAD_OBJECT";
        case AudioRecord.ERROR:
          return "ERROR";
        default:
          return "Unknown (" + errorCode + ")";
      }
    }
  }
}
