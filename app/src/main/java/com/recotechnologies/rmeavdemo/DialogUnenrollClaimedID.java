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

public class DialogUnenrollClaimedID extends AppCompatDialogFragment
{
  private TextInputEditText m_textInputEditTextClaimedID;
  private Switch m_switchUnenrollSpeaker;
  private Switch m_switchUnenrollFace;

  private DialogUnenrollClaimedIDListener m_sDialogUnenrollClaimedIDListener;


  @NonNull
  @Override
  public Dialog onCreateDialog(@Nullable Bundle savedInstanceState)
  {
      AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

      LayoutInflater inflater = getActivity().getLayoutInflater();
      View sView = inflater.inflate(R.layout.dialog_unenroll_claimedid, null);

      builder.setView(sView)
              .setTitle("") // There is no need for a title
              .setNegativeButton("Cancel", new DialogInterface.OnClickListener()
              {
                  @Override
                  public void onClick(DialogInterface dialogInterface, int which)
                  {
                    m_sDialogUnenrollClaimedIDListener.applyUnenrollTexts("",
                                                                          true,     // UnenrollSpeaker
                                                                          false);   // UnenrollFace
                  }
              })
              .setPositiveButton("Unenroll", new DialogInterface.OnClickListener()
              {
                  @Override
                  public void onClick(DialogInterface dialogInterface, int which)
                   {
                    String strClaimedID = m_textInputEditTextClaimedID.getText().toString();
                    Boolean bUnenrollSpeaker = m_switchUnenrollSpeaker.isChecked();
                    Boolean bUnenrollFace = m_switchUnenrollFace.isChecked();

                    m_sDialogUnenrollClaimedIDListener.applyUnenrollTexts(strClaimedID,
                                                                          bUnenrollSpeaker,
                                                                          bUnenrollFace);
                  }
              });

      m_textInputEditTextClaimedID = sView.findViewById(R.id.textInputDialogUnenrollClaimedID);
      m_switchUnenrollSpeaker = (Switch) sView.findViewById(R.id.switchInputDialogUnenrollSpeaker);
      m_switchUnenrollFace = (Switch) sView.findViewById(R.id.switchInputDialogUnenrollFace);

      return builder.create();
  }

    @Override
    public void onAttach(@NonNull Context context)
    {
        super.onAttach(context);
        try
        {
          m_sDialogUnenrollClaimedIDListener = (DialogUnenrollClaimedIDListener) context;
        }
        catch (ClassCastException e)
        {
          throw new ClassCastException(context.toString() +
                                       "You need to implement DialogUnenrollClaimedIDListener!");
        }

    }

  public interface DialogUnenrollClaimedIDListener
  {
    void applyUnenrollTexts(String strClaimedID, Boolean bUnenrollSpeaker, Boolean bUnenrollFace);
  }
}
