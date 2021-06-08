// This header may not be removed.  It should accompany all source code
// derived from this code in any form or fashion.
// Filename: RMEAVImageAndroid.java
// Author: Homayoon Beigi <beigi@recotechnologies.com>
// Copyright (c) 2003-2020 Recognition Technologies, Inc.
// Date: November 21, 2020
// This code may be used as is and may not be distributed or copied without the
// explicit knowledge and permission of the author or Recognition Technologies, Inc.

package com.recotechnologies.rmeavdemo;

import com.recotechnologies.rmeavapi.RMEAVLocal;
import com.recotechnologies.rmeavapi.RMEImageEventListener;
import com.recotechnologies.rmeavapi.RMEImage;

public class RMEImageAndroid extends RMEImage
{
  public RMEImageAndroid(RMEImageEventListener sImageEventListener,
                         int iISCDebug)
  {
    super(sImageEventListener, iISCDebug);

    return;
  }

  public void StartCapture(int iFramePeriod, long hAVEngineHandle)
  {
    try
    {
      Thread.sleep(50);
    } catch (InterruptedException e)
    {
      e.printStackTrace();
    }
    super.StartCapture(iFramePeriod, hAVEngineHandle);

    return;
  }

  public void StopCapture(long hAVEngineHandle)
  {
    super.StopCapture(hAVEngineHandle);

    return;
  }
}
