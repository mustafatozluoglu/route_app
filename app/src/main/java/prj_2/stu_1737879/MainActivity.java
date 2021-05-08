package prj_2.stu_1737879;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText et_startAddress;
    private EditText et_destinationAddress;

    private Button bt_startNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        et_startAddress = (EditText) findViewById(R.id.et_startAddress);
        et_destinationAddress = (EditText) findViewById(R.id.et_destinationAddress);

        bt_startNavigation = (Button) findViewById(R.id.bt_startNavigation);
        bt_startNavigation.setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_startNavigation:
                if (!et_startAddress.getText().toString().isEmpty() && !et_destinationAddress.getText().toString().isEmpty()) {
                    Intent intent = new Intent(getBaseContext(), NavigationActivity.class);
                    Log.d("Start Address:", et_startAddress.getText().toString());
                    Log.d("Destination Address:", et_destinationAddress.getText().toString());
                    //pass value to navigation activity
                    intent.putExtra("startAddress", et_startAddress.getText().toString());
                    intent.putExtra("destinationAddress", et_destinationAddress.getText().toString());

                    startActivity(intent);
                } else {
                    Toast.makeText(getApplicationContext(), "Fill the start and destination address correctly!", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}
