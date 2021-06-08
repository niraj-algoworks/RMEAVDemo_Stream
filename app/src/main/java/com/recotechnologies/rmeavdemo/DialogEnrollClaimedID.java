package com.recotechnologies.rmeavdemo;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Switch;

import com.google.android.material.textfield.TextInputEditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

public class DialogEnrollClaimedID extends AppCompatDialogFragment
{
  private TextInputEditText m_textInputEditTextClaimedID;
  private TextInputEditText m_textInputEditTextClassDescription;
  private Switch m_switchEnrollSpeaker;
  private Switch m_switchEnrollFace;

  private DialogEnrollClaimedIDListener m_sDialogEnrollClaimedIDListener;


  @NonNull
  @Override
  public Dialog onCreateDialog(@Nullable Bundle savedInstanceState)
  {
      AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

      LayoutInflater inflater = getActivity().getLayoutInflater();
      View sView = inflater.inflate(R.layout.dialog_enroll_claimedid, null);

      builder.setView(sView)
              .setTitle("") // There is no need for a title
              .setNegativeButton("Cancel", new DialogInterface.OnClickListener()
              {
                @Override
                public void onClick(DialogInterface dialogInterface, int which)
                {
                  m_sDialogEnrollClaimedIDListener.applyEnrollTexts("", "",
                                                                    true, // EnrollSpeaker
                                                                    false); // EnrollFace
                }
              })
              .setPositiveButton("Enroll", new DialogInterface.OnClickListener()
              {
                @Override
                public void onClick(DialogInterface dialogInterface, int which)
                 {
                  String strClaimedID = m_textInputEditTextClaimedID.getText().toString();
                  String strClassDescription = m_textInputEditTextClassDescription.getText().toString();
                  Boolean bEnrollSpeaker = m_switchEnrollSpeaker.isChecked();
                  Boolean bEnrollFace = m_switchEnrollFace.isChecked();

                  m_sDialogEnrollClaimedIDListener.applyEnrollTexts(strClaimedID,
                                                                    strClassDescription,
                                                                    bEnrollSpeaker,
                                                                    bEnrollFace);
                }
              });

      m_textInputEditTextClaimedID = sView.findViewById(R.id.textInputDialogEnrollClaimedID);
      m_textInputEditTextClassDescription = sView.findViewById(R.id.textInputDialogEnrollClassDescription);
      m_switchEnrollSpeaker = (Switch) sView.findViewById(R.id.switchInputDialogEnrollSpeaker);
      m_switchEnrollFace = (Switch) sView.findViewById(R.id.switchInputDialogEnrollFace);

      return builder.create();
  }

    @Override
    public void onAttach(@NonNull Context context)
    {
        super.onAttach(context);
        try
        {
          m_sDialogEnrollClaimedIDListener = (DialogEnrollClaimedIDListener) context;
        }
        catch (ClassCastException e)
        {
          throw new ClassCastException(context.toString() +
                                       "You need to implement DialogEnrollClaimedIDListener!");
        }

    }

  public interface DialogEnrollClaimedIDListener
  {
    void applyEnrollTexts(String strClaimedID, String strClassDescription,
                          Boolean bEnrollSpeaker, Boolean bEnrollFace);
  }
}
