package com.lt.jitpacktest.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.lt.jitpacktest.R;
import com.lt.jitpacktest.fragment.GameFragment;
import com.lt.jitpacktest.fragment.PartInFragment;
import com.lt.jitpacktest.fragment.RewardFragment;
import com.lt.jitpacktest.tools.TabPageAdapter;
import com.lt.jitpacktest.tools.customViewPager;
import com.lt.jitpacktest.utils.SessionSingleton;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class YunBuNavigateActivity extends AppCompatActivity implements View.OnClickListener {
    private LinearLayout ll_yunbu_game, ll_yunbu_reward, ll_yunbu_partin;
    private LinearLayout ll_navigation_game_bgcolor, ll_navigation_reward_bgcolor, ll_navigation_partin_bgcolor;
    private ImageView iv_game, iv_reward, iv_partin;
    private TextView tv_game, tv_reward, tv_partin;

    private ImageView iv_main_back;
    private TextView tv_navigate_title, tv_navigate_title_withdrawal;
    private RelativeLayout rl_main_background;

    private customViewPager viewpager;
    int[] unselectedIconIds;
    int[] selectedIconIds;
    List<Fragment> fragments = new ArrayList<Fragment>();

    private int showTypeGame = 0, showTypeReward = 0;

    private String withdrawType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null){
            actionBar.hide();
        }
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            //decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        setContentView(R.layout.activity_yun_bu_navigate);

        initview();

    }


    private void initview() {

        ll_yunbu_game = findViewById(R.id.ll_yunbu_game);
        ll_yunbu_partin = findViewById(R.id.ll_yunbu_partin);
        ll_yunbu_reward = findViewById(R.id.ll_yunbu_reward);

        ll_navigation_game_bgcolor = findViewById(R.id.ll_navigation_game_bgcolor);
        ll_navigation_reward_bgcolor = findViewById(R.id.ll_navigation_reward_bgcolor);
        ll_navigation_partin_bgcolor = findViewById(R.id.ll_navigation_partin_bgcolor);

        iv_game = findViewById(R.id.homeimage);
        iv_reward = findViewById(R.id.rewardimage);
        iv_partin = findViewById(R.id.partinimage);


        tv_game = findViewById(R.id.hometext);
        tv_reward = findViewById(R.id.rewardtext);
        tv_partin = findViewById(R.id.partintext);

        iv_main_back = findViewById(R.id.iv_main_back);
        tv_navigate_title = findViewById(R.id.tv_navigate_title);
        tv_navigate_title_withdrawal = findViewById(R.id.tv_navigate_title_withdrawal);
        rl_main_background = findViewById(R.id.rl_main_background);

        if (SessionSingleton.getInstance().hasStyleConfig == 1) {
            tv_navigate_title.setText(SessionSingleton.getInstance().mYBStyleConfig.getTitleText());
            tv_navigate_title.setTextColor(SessionSingleton.getInstance().mYBStyleConfig.getTitleTextColor());
            rl_main_background.setBackgroundColor(SessionSingleton.getInstance().mYBStyleConfig.getTitleBackColor());

            if(SessionSingleton.getInstance().mYBStyleConfig.getTitleBackIcon()==0){
                iv_main_back.setImageDrawable(getResources().getDrawable(R.mipmap.ic_yunbu_back_black));
            }else{
                iv_main_back.setImageDrawable(getResources().getDrawable(R.mipmap.ic_yunbu_back_write));
            }

            ll_navigation_game_bgcolor.setBackgroundColor(SessionSingleton.getInstance().mYBStyleConfig.getNavigateTextColor());
            ll_navigation_reward_bgcolor.setBackgroundColor(SessionSingleton.getInstance().mYBStyleConfig.getNavigateTextColor());
            ll_navigation_partin_bgcolor.setBackgroundColor(SessionSingleton.getInstance().mYBStyleConfig.getNavigateTextColor());
        }


        ll_yunbu_game.setOnClickListener(this);
        ll_yunbu_reward.setOnClickListener(this);
        ll_yunbu_partin.setOnClickListener(this);

        try {
            String showTabSign = SessionSingleton.getInstance().AccountSingle.getString("showTabSign");

            withdrawType = SessionSingleton.getInstance().AccountSingle.getString("withdrawType");

            //todo
             //showTabSign = "游戏";
            if (showTabSign.contains("悬赏")) {
                showTypeReward = 1;
            }

            if (showTabSign.contains("游戏")) {
                showTypeGame = 1;
            }

            if (withdrawType.equals("平台自提")) {
                if (SessionSingleton.getInstance().hasStyleConfig == 1) {
                    tv_navigate_title_withdrawal.setVisibility(View.GONE);
                }else{
                    tv_navigate_title_withdrawal.setVisibility(View.GONE);
                }
            } else {
                if (SessionSingleton.getInstance().hasStyleConfig == 1) {
                    tv_navigate_title_withdrawal.setVisibility(View.VISIBLE);
                    tv_navigate_title_withdrawal.setTextColor(SessionSingleton.getInstance().mYBStyleConfig.getTitleTextColor());
                }else{
                    tv_navigate_title_withdrawal.setVisibility(View.VISIBLE);
                }
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }


        checkNeedPermissions();

        tv_navigate_title_withdrawal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(YunBuNavigateActivity.this, YunBuWithdrawalActivity.class);
                startActivity(intent);
            }
        });

        iv_main_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


    }

    @Override
    public void onClick(View v) {

        int id = v.getId();
        if (id == R.id.ll_yunbu_game) {
            iv_game.setImageDrawable(getResources().getDrawable(R.mipmap.ic_yunbu_game_select));
            iv_reward.setImageDrawable(getResources().getDrawable(R.mipmap.ic_yunbu_reward_unselect));
            iv_partin.setImageDrawable(getResources().getDrawable(R.mipmap.ic_yunbu_partin_unselect));

            if (SessionSingleton.getInstance().hasStyleConfig == 1) {
                tv_game.setTextColor(SessionSingleton.getInstance().mYBStyleConfig.getNavigateTextColor());
            }else{
                tv_game.setTextColor(getResources().getColor(R.color.yunbu_green));
            }
            tv_reward.setTextColor(getResources().getColor(R.color.yunbu_textgray));
            tv_partin.setTextColor(getResources().getColor(R.color.yunbu_textgray));

            if (showTypeReward == 1 && showTypeGame == 1) {
                viewpager.setCurrentItem(0, false);
            } else if (showTypeReward == 1 && showTypeGame != 1) {
                viewpager.setCurrentItem(0, false);
            } else if (showTypeReward != 1 && showTypeGame == 1) {
                viewpager.setCurrentItem(0, false);
            }

        } else if (id == R.id.ll_yunbu_reward) {
            iv_game.setImageDrawable(getResources().getDrawable(R.mipmap.ic_yunbu_game_unselect));
            iv_reward.setImageDrawable(getResources().getDrawable(R.mipmap.ic_yunbu_reward_select));
            iv_partin.setImageDrawable(getResources().getDrawable(R.mipmap.ic_yunbu_partin_unselect));

            tv_game.setTextColor(getResources().getColor(R.color.yunbu_textgray));
            if (SessionSingleton.getInstance().hasStyleConfig == 1) {
                tv_reward.setTextColor(SessionSingleton.getInstance().mYBStyleConfig.getNavigateTextColor());
            }else{
                tv_reward.setTextColor(getResources().getColor(R.color.yunbu_green));
            }
            tv_partin.setTextColor(getResources().getColor(R.color.yunbu_textgray));

            if (showTypeReward == 1 && showTypeGame == 1) {
                viewpager.setCurrentItem(1, false);
            } else if (showTypeReward == 1 && showTypeGame != 1) {
                viewpager.setCurrentItem(0, false);
            } else if (showTypeReward != 1 && showTypeGame == 1) {
                viewpager.setCurrentItem(0, false);
            }
        } else if (id == R.id.ll_yunbu_partin) {
            iv_game.setImageDrawable(getResources().getDrawable(R.mipmap.ic_yunbu_game_unselect));
            iv_reward.setImageDrawable(getResources().getDrawable(R.mipmap.ic_yunbu_reward_unselect));
            iv_partin.setImageDrawable(getResources().getDrawable(R.mipmap.ic_yunbu_partin_select));

            tv_game.setTextColor(getResources().getColor(R.color.yunbu_textgray));
            tv_reward.setTextColor(getResources().getColor(R.color.yunbu_textgray));

            if (SessionSingleton.getInstance().hasStyleConfig == 1) {
                tv_partin.setTextColor(SessionSingleton.getInstance().mYBStyleConfig.getNavigateTextColor());
            }else{
                tv_partin.setTextColor(getResources().getColor(R.color.yunbu_green));
            }

            if (showTypeReward == 1 && showTypeGame == 1) {
                viewpager.setCurrentItem(2, false);
            } else if (showTypeReward == 1 && showTypeGame != 1) {
                viewpager.setCurrentItem(1, false);
            } else if (showTypeReward != 1 && showTypeGame == 1) {
                viewpager.setCurrentItem(1, false);
            }
        }

    }

    private void checkNeedPermissions() {
        String[] PERMISSIONS_STORAGE = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        int permission = ActivityCompat.checkSelfPermission(YunBuNavigateActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    YunBuNavigateActivity.this,
                    PERMISSIONS_STORAGE,
                    1
            );
        } else {

            if (showTypeReward == 1 && showTypeGame == 1) {
                unselectedIconIds = new int[]{R.mipmap.ic_yunbu_game_unselect, R.mipmap.ic_yunbu_reward_unselect, R.mipmap.ic_yunbu_partin_unselect};
                selectedIconIds = new int[]{R.mipmap.ic_yunbu_game_select, R.mipmap.ic_yunbu_reward_select, R.mipmap.ic_yunbu_partin_select};

                //默认选中首页
                iv_game.setImageDrawable(getResources().getDrawable(R.mipmap.ic_yunbu_game_select));

                if (SessionSingleton.getInstance().hasStyleConfig == 1) {
                    tv_game.setTextColor(SessionSingleton.getInstance().mYBStyleConfig.getNavigateTextColor());
                }else{
                    tv_game.setTextColor(getResources().getColor(R.color.yunbu_green));
                }
                tv_reward.setTextColor(getResources().getColor(R.color.yunbu_textgray));
                tv_partin.setTextColor(getResources().getColor(R.color.yunbu_textgray));

                ll_yunbu_game.setVisibility(View.VISIBLE);
                ll_yunbu_reward.setVisibility(View.VISIBLE);
                ll_yunbu_partin.setVisibility(View.VISIBLE);

                //将fragment放入范型
                Fragment GameFragment = new GameFragment();
                Fragment RewardFragment = new RewardFragment();
                Fragment PartInFragment = new PartInFragment();


                fragments.add(GameFragment);
                fragments.add(RewardFragment);
                fragments.add(PartInFragment);


                viewpager = findViewById(R.id.home_viewpager);
                viewpager.setPagingEnabled(false);
                TabPageAdapter tabPageAdapter = new TabPageAdapter(getSupportFragmentManager(), fragments);
                viewpager.setAdapter(tabPageAdapter);

                fragments = null;
            } else if (showTypeReward == 1 && showTypeGame != 1) {

                unselectedIconIds = new int[]{R.mipmap.ic_yunbu_reward_unselect, R.mipmap.ic_yunbu_partin_unselect};
                selectedIconIds = new int[]{R.mipmap.ic_yunbu_reward_select, R.mipmap.ic_yunbu_partin_select};

                //默认选中首页
                iv_reward.setImageDrawable(getResources().getDrawable(R.mipmap.ic_yunbu_reward_select));


                if (SessionSingleton.getInstance().hasStyleConfig == 1) {
                    tv_reward.setTextColor(SessionSingleton.getInstance().mYBStyleConfig.getNavigateTextColor());
                }else{
                    tv_reward.setTextColor(getResources().getColor(R.color.yunbu_green));
                }
                tv_game.setTextColor(getResources().getColor(R.color.yunbu_textgray));
                tv_partin.setTextColor(getResources().getColor(R.color.yunbu_textgray));


                ll_yunbu_game.setVisibility(View.GONE);
                ll_yunbu_reward.setVisibility(View.VISIBLE);
                ll_yunbu_partin.setVisibility(View.VISIBLE);

                //将fragment放入范型
                Fragment RewardFragment = new RewardFragment();
                Fragment PartInFragment = new PartInFragment();


                fragments.add(RewardFragment);
                fragments.add(PartInFragment);


                viewpager = findViewById(R.id.home_viewpager);
                viewpager.setPagingEnabled(false);
                TabPageAdapter tabPageAdapter = new TabPageAdapter(
                        getSupportFragmentManager(), fragments);
                viewpager.setAdapter(tabPageAdapter);

                fragments = null;
            } else if (showTypeReward != 1 && showTypeGame == 1) {

                unselectedIconIds = new int[]{R.mipmap.ic_yunbu_game_unselect, R.mipmap.ic_yunbu_partin_unselect};
                selectedIconIds = new int[]{R.mipmap.ic_yunbu_game_select, R.mipmap.ic_yunbu_partin_select};

                //默认选中首页
                iv_game.setImageDrawable(getResources().getDrawable(R.mipmap.ic_yunbu_game_select));


                if (SessionSingleton.getInstance().hasStyleConfig == 1) {
                    tv_game.setTextColor(SessionSingleton.getInstance().mYBStyleConfig.getNavigateTextColor());
                }else{
                    tv_game.setTextColor(getResources().getColor(R.color.yunbu_green));
                }
                tv_reward.setTextColor(getResources().getColor(R.color.yunbu_textgray));
                tv_partin.setTextColor(getResources().getColor(R.color.yunbu_textgray));


                ll_yunbu_game.setVisibility(View.VISIBLE);
                ll_yunbu_reward.setVisibility(View.GONE);
                ll_yunbu_partin.setVisibility(View.VISIBLE);

                //将fragment放入范型
                Fragment GameFragment = new GameFragment();
                Fragment PartInFragment = new PartInFragment();


                fragments.add(GameFragment);
                fragments.add(PartInFragment);


                viewpager = findViewById(R.id.home_viewpager);
                viewpager.setPagingEnabled(false);
                TabPageAdapter tabPageAdapter = new TabPageAdapter(
                        getSupportFragmentManager(), fragments);
                viewpager.setAdapter(tabPageAdapter);

                fragments = null;
            }


        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            if (showTypeReward == 1 && showTypeGame == 1) {
                unselectedIconIds = new int[]{R.mipmap.ic_yunbu_game_unselect, R.mipmap.ic_yunbu_reward_unselect, R.mipmap.ic_yunbu_partin_unselect};
                selectedIconIds = new int[]{R.mipmap.ic_yunbu_game_select, R.mipmap.ic_yunbu_reward_select, R.mipmap.ic_yunbu_partin_select};

                //默认选中首页
                iv_game.setImageDrawable(getResources().getDrawable(R.mipmap.ic_yunbu_game_select));

                ll_yunbu_game.setVisibility(View.VISIBLE);
                ll_yunbu_reward.setVisibility(View.VISIBLE);
                ll_yunbu_partin.setVisibility(View.VISIBLE);

                //将fragment放入范型
                Fragment GameFragment = new GameFragment();
                Fragment RewardFragment = new RewardFragment();
                Fragment PartInFragment = new PartInFragment();


                fragments.add(GameFragment);
                fragments.add(RewardFragment);
                fragments.add(PartInFragment);


                viewpager = findViewById(R.id.home_viewpager);
                viewpager.setPagingEnabled(false);
                TabPageAdapter tabPageAdapter = new TabPageAdapter(getSupportFragmentManager(), fragments);
                viewpager.setAdapter(tabPageAdapter);

                fragments = null;
            } else if (showTypeReward == 1 && showTypeGame != 1) {

                unselectedIconIds = new int[]{R.mipmap.ic_yunbu_reward_unselect, R.mipmap.ic_yunbu_partin_unselect};
                selectedIconIds = new int[]{R.mipmap.ic_yunbu_reward_select, R.mipmap.ic_yunbu_partin_select};

                //默认选中首页
                iv_reward.setImageDrawable(getResources().getDrawable(R.mipmap.ic_yunbu_reward_select));


                ll_yunbu_game.setVisibility(View.GONE);
                ll_yunbu_reward.setVisibility(View.VISIBLE);
                ll_yunbu_partin.setVisibility(View.VISIBLE);

                //将fragment放入范型
                Fragment RewardFragment = new RewardFragment();
                Fragment PartInFragment = new PartInFragment();


                fragments.add(RewardFragment);
                fragments.add(PartInFragment);


                viewpager = findViewById(R.id.home_viewpager);
                viewpager.setPagingEnabled(false);
                TabPageAdapter tabPageAdapter = new TabPageAdapter(
                        getSupportFragmentManager(), fragments);
                viewpager.setAdapter(tabPageAdapter);

                fragments = null;
            } else if (showTypeReward != 1 && showTypeGame == 1) {

                unselectedIconIds = new int[]{R.mipmap.ic_yunbu_game_unselect, R.mipmap.ic_yunbu_partin_unselect};
                selectedIconIds = new int[]{R.mipmap.ic_yunbu_game_select, R.mipmap.ic_yunbu_partin_select};

                //默认选中首页
                iv_game.setImageDrawable(getResources().getDrawable(R.mipmap.ic_yunbu_game_select));


                ll_yunbu_game.setVisibility(View.VISIBLE);
                ll_yunbu_reward.setVisibility(View.GONE);
                ll_yunbu_partin.setVisibility(View.VISIBLE);

                //将fragment放入范型
                Fragment GameFragment = new GameFragment();
                Fragment PartInFragment = new PartInFragment();


                fragments.add(GameFragment);
                fragments.add(PartInFragment);


                viewpager = findViewById(R.id.home_viewpager);
                viewpager.setPagingEnabled(false);
                TabPageAdapter tabPageAdapter = new TabPageAdapter(
                        getSupportFragmentManager(), fragments);
                viewpager.setAdapter(tabPageAdapter);

                fragments = null;
            }


        } else {
            // 没有获取 到权限，从新请求，或者关闭app
            Toast.makeText(this, "需要存储权限", Toast.LENGTH_SHORT).show();

            finish();
        }
    }

}
