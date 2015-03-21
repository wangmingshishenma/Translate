package com.baidu.baidutranslate.openapi;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.baidu.baidutranslate.openapi.FloatingActionButton;
import com.baidu.baidutranslate.openapi.adapter.FromToLangAdapter;
import com.baidu.baidutranslate.openapi.callback.IDictResultCallback;
import com.baidu.baidutranslate.openapi.callback.ITransResultCallback;
import com.baidu.baidutranslate.openapi.entity.DictMean;
import com.baidu.baidutranslate.openapi.entity.DictResult;
import com.baidu.baidutranslate.openapi.entity.Exchange;
import com.baidu.baidutranslate.openapi.entity.Symbol;
import com.baidu.baidutranslate.openapi.entity.TransResult;
import com.baidu.baidutranslate.openapi.util.Utils;

public class MainActivity extends Activity implements View.OnClickListener {

    // TODO 【重要】将API_KEY换成自己的
    // API_KEY获取地址 http://developer.baidu.com/console
    // 该API_KEY只是初级权限，如果出现翻译不了的情况，请将API_KEY更换为自己的
    public static final String API_KEY = "LIQEr1K31TyMEaP4RhN5zo72";

    private TranslateClient client;// 翻译、词典接口

    private EditText contentEditText;
    private TextView translateResutlText;
    private TextView translateDebugText;
    private Spinner fromSpinner;
    private Spinner toSpinner;

    private FromToLangAdapter fromAdapter;
    private FromToLangAdapter toAdapter;

    private String fromLang = "auto";
    private String toLang = "auto";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new FloatingActionButton.Builder(this)
    	.withButtonColor(getResources().getColor(R.color.blue))
    	.withButtonSize(85)
    	.withDrawable(getResources().getDrawable(R.drawable.ic_add_white_24dp))
    	.withGravity(Gravity.RIGHT|Gravity.CENTER)
    	.withPaddings(0, 0, 10, 10)
    	.create()
    	.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					 hideSoftInput();
		             translate();
				}
			});
        initTransClient();// 初始化翻译相关功能
        initView();// 初始化界面
        initLang();// 初始化语音方向
    }

    // 【重要】 onCreate时候初始化翻译相关功能
    private void initTransClient() {
        client = new TranslateClient(this, API_KEY);
        TranslateClient.enableLog(true);

    }

    private void initLang() {
        fromAdapter = new FromToLangAdapter(this);
        toAdapter = new FromToLangAdapter(this);

        fromSpinner.setAdapter(fromAdapter);
        toSpinner.setAdapter(toAdapter);

        fromSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                fromLang = fromAdapter.getLang(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                fromLang = "auto";
            }
        });

        toSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                toLang = fromAdapter.getLang(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                toLang = "auto";
            }
        });
    }

    private void initView() {

        contentEditText = (EditText) findViewById(R.id.source_content_text);
        translateResutlText = (TextView) findViewById(R.id.translate_result_text);
        translateDebugText = (TextView) findViewById(R.id.translate_debug_text);
        fromSpinner = (Spinner) findViewById(R.id.from_lang_spinner);
        toSpinner = (Spinner) findViewById(R.id.to_lang_spinner);

        findViewById(R.id.translate_btn).setOnClickListener(this);
        findViewById(R.id.translate_btn).setOnClickListener(this);
    }

    // onDestroy时候注销掉翻译功能(可以不调用)
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (client != null) {
            client.onDestroy();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.translate_btn:
                hideSoftInput();
                translate();
                break;

           /* case R.id.translate_btn:
                hideSoftInput();
                dict();
                break;*/

            default:
                break;
        }

    }

    private void translate() {

        String content = contentEditText.getText().toString();// 原文
        if (TextUtils.isEmpty(content))
            return;

        // 【重要】翻译功能调用 4个参数分别为 原文、源语言方向、目标语言方向、回调
        client.translate(content, fromLang, toLang, new ITransResultCallback() {

            @Override
            public void onResult(TransResult result) {// 翻译结果回调
                if (result == null) {
                    Log.d("TransOpenApiDemo", "Trans Result is null");

                } else {
                    Log.d("TransOpenApiDemo", "MainActivity->" + result.toJSONString());

                    translateDebugText.setText(result.toJSONString());
                    if (result.error_code == 0) {// 没错
                        translateResutlText.setText(result.trans_result);
                    } else {
                        translateResutlText.setText(result.error_msg);
                    }
                }
            }
        });
    }

    private void dict() {

        String content = contentEditText.getText().toString();// 原文
        if (TextUtils.isEmpty(content))
            return;

        // 【重要】cidian 功能调用 4个参数分别为 原文、源语言方向、目标语言方向、回调
        client.dict(content, fromLang, toLang, new IDictResultCallback() {

            @Override
            public void onResult(DictResult result) {// 翻译结果回调
                if (result == null) {
                    Log.e("TransOpenApiDemo", "Trans Result is null");

                } else {
                    translateDebugText.setText(result.toJSONString());
                    if (result.error_code == 0) {// 没错
                        translateResutlText.setText(buildDictResult(result));
                    } else {
                        translateResutlText.setText(R.string.error_occurred);
                    }
                }
            }
        });
    }

    private void hideSoftInput() {
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(contentEditText.getWindowToken(), 0);
    }

    private String buildDictResult(DictResult dict) {
        StringBuilder builder = new StringBuilder();

        String word_name = dict.word_name;// 原文
        builder.append(word_name);
        builder.append("\n");

        List<Symbol> symbols = dict.symbols;// 发音以及释义
        for (int i = 0; i < symbols.size(); i++) {
            Symbol symbol = symbols.get(i);

            // 发音相关
            if (!TextUtils.isEmpty(symbol.ph_am)) {
                builder.append("美式发音[" + symbol.ph_am + "]");
            }
            if (!TextUtils.isEmpty(symbol.ph_en)) {
                builder.append("英式发音[" + symbol.ph_en + "]");
            }
            if (!TextUtils.isEmpty(symbol.ph_zh)) {
                builder.append("拼音[" + symbol.ph_zh + "]");
            }
            builder.append("\n");

            // 在该发音时候的释义
            List<DictMean> dict_means = symbol.dict_means;
            for (int j = 0; j < dict_means.size(); j++) {
                DictMean dict_mean = dict_means.get(j);

                if (!TextUtils.isEmpty(dict_mean.part)) {// 词性（名词、形容词、动词等）
                    builder.append("词性：" + dict_mean.part);
                    builder.append("\n");
                }

                builder.append("词义：");
                List<String> means = dict_mean.means;// 词义
                for (int k = 0; k < means.size(); k++) {
                    builder.append(means.get(k) + ";");// 多个词义用;分割
                }
                builder.append("\n");
            }

            builder.append("\n");
        }

        // 拓展属性(第三人称、比较级、过去式等)
        Exchange exchage = dict.exchange;
        if (exchage != null) {
            if (!Utils.isEmpty(exchage.word_ing)) {// 进行时
                builder.append("进行时:" + exchage.word_ing + " ");
            }
            if (!Utils.isEmpty(exchage.word_past)) {// 过去式
                builder.append("过去式:" + exchage.word_past + " ");
            }
            if (!Utils.isEmpty(exchage.word_done)) {// 过去分词
                builder.append("过去分词:" + exchage.word_done + " ");
            }
            if (!Utils.isEmpty(exchage.word_er)) {// 比较级
                builder.append("比较级:" + exchage.word_er + " ");
            }
            if (!Utils.isEmpty(exchage.word_est)) {// 最高级
                builder.append("最高级:" + exchage.word_est + " ");
            }
            if (!Utils.isEmpty(exchage.word_pl)) {// 复数
                builder.append("复数:" + exchage.word_pl + " ");
            }
            if (!Utils.isEmpty(exchage.word_third)) {// 第三人称
                builder.append("第三人称:" + exchage.word_third + " ");
            }
        }
        return builder.toString();
    
    }
    
}
