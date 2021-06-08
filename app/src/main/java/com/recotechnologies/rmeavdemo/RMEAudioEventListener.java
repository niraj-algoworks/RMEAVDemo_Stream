package com.recotechnologies.rmeavdemo;

public interface RMEAudioEventListener
{
  int AVEnginePushPCMAudioDataByteArray(long hAVEngineHandle,
                                        byte[] abMedia,
                                        int iNumSample,
                                        int iSerialNum,
                                        String strJSONMetaData);

  int AVEngineCompletePCMAudio(long hAVEngineHandle,
                               int iSerialNum);

  // 0 -- not in progress
  // 1 -- in progress
  // 2 -- completed
  int SetRecordingInProgress(long hAVEngineHandle,
                             int iRecordingInProgress);
}
