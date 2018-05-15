package com.example.huangdi.cs572hw9;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.EditText;
import android.widget.TextView;

public class search_page extends Activity {

    private RadioGroup r_group;
    private RadioButton from_cur;
    private RadioButton from_other;
    private EditText location_input;
    private TextView keyword_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.f1);
      //  getSupportActionBar().setElevation(0);

         location_input = (EditText) findViewById(R.id.location_input);
      //  keyword_input= (EditText) findViewById(R.id.keyword_input);
      //  keyword_input.setText("hello");
        keyword_text=(TextView) findViewById(R.id.keyword_text);
        keyword_text.setText("hello");

        r_group=(RadioGroup)this.findViewById(R.id.from_rg);
        from_cur=(RadioButton)this.findViewById(R.id.from_current);
        from_other=(RadioButton)this.findViewById(R.id.from_other);
        r_group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(from_cur.getId()==checkedId){   // from_current checked
                    location_input.setEnabled(false);
                }else{
                    location_input.setEnabled(true);
                }

            }
        });

}
}
