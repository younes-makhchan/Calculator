package com.translator.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

//import android.util.TypedValue;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;


public class MainActivity extends AppCompatActivity implements View.OnClickListener  {
    TextView result,solution;
    List<MaterialButton> buttons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        result=findViewById(R.id.result);
        solution=findViewById(R.id.solution);
        if(savedInstanceState!=null){
            result.setText(savedInstanceState.getString("result"));
            solution.setText(savedInstanceState.getString("solution"));

        }
        //populate buttons list
        int numButtons=10;
        buttons=new ArrayList<>();
        String specialButtons[]={"c","open_bracket","close_bracket","ac","plus","devider","multiple","minus","equal","cos","sin","tan"};
        for(int i=0;i<numButtons;i++){
            int buttonId=getResources().getIdentifier("button_"+i,"id",getPackageName());
            buttons.add(assignedId(buttonId));
        }
        for(String field:specialButtons){
            int buttonId=getResources().getIdentifier("button_"+field,"id",getPackageName());
            buttons.add(assignedId(buttonId));
        }
    }
    MaterialButton assignedId(int id){
        MaterialButton btn=findViewById(id);
        btn.setOnClickListener(this);
        return btn;
    }

    @Override
    public void onClick(View view) {
        MaterialButton btn = (MaterialButton)view;
        String  btnText=btn.getText().toString();
        String solutionText=solution.getText().toString();
        String operators = "+-*/";
        String functions = "sinct"; // Assuming the input sequence might contain s, i, n, c, or t for sin, cos, tan
        if (operators.contains(String.valueOf(btnText.charAt(0))) && (operators.contains(String.valueOf(solutionText.charAt(solutionText.length() - 1)))|| solutionText.equals("0"))) {
            //ignore
        }
        else if(btnText.equals("C")){
            if(solutionText.length()!=0){
                String newSolutionText="";
                if(solutionText.endsWith("tan(")||solutionText.endsWith("cos(")||solutionText.endsWith("sin(")){
                     newSolutionText=solutionText.substring(0,solutionText.length()-4);

                }else{
                     newSolutionText=solutionText.substring(0,solutionText.length()-1);
                }
                solution.setText(newSolutionText.length()==0?"0":newSolutionText);

            }
        }else if (btnText.equals("AC")){
            solution.setText("0");
            result.setText("0");
        }else if(btnText.equals("=")){
            solution.setText(result.getText());
        }else{

            //update btn text if it's cos tan sin
            if(btnText.equals("cos")||btnText.equals("tan")||btnText.equals("sin")){
                btnText+="(";
            }

            if(solution.getText().equals("0")){
                solution.setText(btnText);
            }else{
                solution.setText(solution.getText()+btnText);
            }

        }
        String resultText=getResult(solution.getText().toString());
        if(!resultText.equals("Err")){
            if(resultText.endsWith(".0")){
                resultText=resultText.replace(".0","");
            }
            if(resultText.length()>=8){
                //change the result text view size
                result.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 35);
            }
            result.setText(resultText);

        }


    }
    String getResult(String data){
        String result;
        try{
            data = calculateTrigonometricFunctions(data);
            Context context = Context.enter();
            context.setOptimizationLevel(-1);
            Scriptable scope = context.initStandardObjects();
            result=context.evaluateString(scope,data,"Javascript",1,null).toString();
        }catch (Exception e){
            return "Err";
        }

        return  result;
    }
    private String calculateTrigonometricFunctions(String data) {
        // Regular expression to match trigonometric function patterns
        String regex = "(tan|sin|cos)\\(([^)]+)\\)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(data);

        // Iterate through matches and replace them with calculated values
        while (matcher.find()) {
            String functionName = matcher.group(1);
            double argument = Double.parseDouble(matcher.group(2));
            double result;
            switch (functionName) {
                case "tan":
                    result = Math.tan(argument);
                    break;
                case "sin":
                    result = Math.sin(argument);
                    break;
                case "cos":
                    result = Math.cos(argument);
                    break;
                default:
                    result = Double.NaN; // Not a Number
            }
            // Replace the matched function with the calculated result
            data = data.replace(matcher.group(), String.valueOf(result));
        }
        return data;
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save the state of your variables to the bundle
        outState.putString("solution", solution.getText().toString());
        outState.putString("result", result.getText().toString());

        // Save other variables and UI components as needed
    }
}