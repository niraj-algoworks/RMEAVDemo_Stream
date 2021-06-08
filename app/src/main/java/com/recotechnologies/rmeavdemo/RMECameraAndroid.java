// This header may not be removed.  It should accompany all source code
// derived from this code in any form or fashion.
// Filename: RMEAVCameraAndroid.java
// Author: Homayoon Beigi <beigi@recotechnologies.com>
// Copyright (c) 2003-2020 Recognition Technologies, Inc.
// Date: November 21, 2020
// This code may be used as is and may not be distributed or copied without the
// explicit knowledge and permission of the author or Recognition Technologies, Inc.

package com.recotechnologies.rmeavdemo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.recotechnologies.rmeavapi.AutoFitTextureView;
import com.recotechnologies.rmeavapi.RMECamera;

public class RMECameraAndroid extends RMECamera
{
  public RMECameraAndroid(RMEImageAndroid sRMEImageAndroid,
                          String strCameraLens,
                          int iWidthRequested,
                          int iHeightRequested,
                          int iNumChannelRequested,
                          int iNumBytePerChannelRequested,
                          int iISCDebug)
  {
    super(sRMEImageAndroid, strCameraLens, iWidthRequested, iHeightRequested,
          iNumChannelRequested, iNumBytePerChannelRequested, iISCDebug);

    return;
  }

  public void CaptureImage(long hRMEAVEngineHandle,
                           String strImageFormat,
                           int iWidthRequested,
                           int iHeightRequested,
                           int iNumChannel,
                           int iNumBytePerChannel)
  {
    super.CaptureImage(hRMEAVEngineHandle,
                       strImageFormat,
                       iWidthRequested,
                       iHeightRequested,
                       iNumChannel,
                       iNumBytePerChannel);

    return;
  }

  @Override
  public View onCreateView(LayoutInflater inflater,
                           ViewGroup cameracontainer,
                           Bundle savedInstanceState)
  {
    return inflater.inflate(R.layout.camera_fragment, cameracontainer, false);
  }

  @Override
  public void onViewCreated(final View view, Bundle savedInstanceState)
  {
    //view.findViewById(R.id.btn_cameracaptureimage).setOnClickListener(this);
    //view.findViewById(R.id.info).setOnClickListener(this);
    mTextureView = (AutoFitTextureView) view.findViewById(R.id.cameratexture);
  }

}
