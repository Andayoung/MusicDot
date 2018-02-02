# MusicDot
百度音乐接口
应用SDK与对外接口的封装、混淆与打包

从事Android开发久了不可避免的会接触对外接口的封装，下面本人就以自己写过的一个例子系统讲讲怎样对应用sdk的开发。

-------------------


### 1.封装

我们在与其他公司合作的时候，往往会有技术方面的协作，但我们又不想过多的暴露我们某些技术方面的细节，比如：ip地址、技术参数、核心算法。

于是我们会对这部分代码进行封装，这部分需要精通Java的设计模式。
下面上传我的项目：

![这里写图片描述](http://img.blog.csdn.net/20180202101313449?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvemhvdXAzMjQ=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)


新建一个Android工程，添加一个Model：musicdot。

musicdot是本人自己解析百度音乐的歌曲搜索请求实现的在线搜歌功能包。所以我将要对这部分代码进行封装打包，以达到隐藏具体搜索请求的目的。（由于本项目中用到的百度音乐搜索接口涉及侵权，暂不暴露）

下面贴代码：

```
public class PlayMusicPresenter {
    private PlayMusicModle uModel;
    public void getMusic(Context context, String content, final UUView uView) {
        uModel = new PlayMusicModle();
        uModel.getResult(context, content, new UCallBack() {
            @Override
            public void onSuccess(String content, int code) {
                uView.showContent(content, code);
            }

            @Override
            public void onFail(String error) {
                uView.showError(error);
            }

        });
    }
}
```

```
public class PlayMusicModle {
    private UCallBack callBack;

    public void getResult(Context context, String content, UCallBack uCallBack) {
        callBack = uCallBack;
        searchSong(content);
    }

    private void searchSong(final String musicName) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .addHeader("User-Agent", makeUA())
                            .url("搜索url" + musicName)
                            .build();
                    Response response = client.newCall(request).execute();
                    if (response.isSuccessful()) {
                        String responseData = response.body().string();
                        searchSongAdd(getSongId(responseData));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    private void searchSongAdd(final String songid) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .addHeader("User-Agent", makeUA())
                            .url("搜索url" + songid)
                            .build();
                    Response response = client.newCall(request).execute();
                    if (response.isSuccessful()) {
                        String responseData = response.body().string();
                        callBack.onSuccess(getSongAddUrl(responseData), 0);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    private String getSongId(String jsonData) {
        String id = "";
        id = jsonData.substring(jsonData.indexOf("songid\":\"") + 9, jsonData.indexOf("\",\"has_mv"));
        return id;
    }

    private String getSongAddUrl(String songAdd) {
        String add = "";
        add = songAdd.substring(songAdd.indexOf("show_link\":\"") + 12, songAdd.indexOf("\",\"free"));
        add = add.replace("\\", "");
        return add;
    }

    private String makeUA() {
        final String ua = Build.BRAND + "/" + Build.MODEL + "/" + Build.VERSION.RELEASE;
        return ua;
    }
```

上面Model中的两片主体代码是典型的MVP模式，思路清晰，不多做解释。下面贴上使用代码：

```
public class MainActivity extends AppCompatActivity{
    MediaPlayer mp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mp=new MediaPlayer();
        PlayMusicPresenter playMusicPresenter = new PlayMusicPresenter();
        playMusicPresenter.getMusic(MainActivity.this, "稻香", new UUView() {
            @Override
            public void showContent(String content, int code) {
                mp.reset();
                try {
                    mp.setDataSource(content);
                    mp.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mp.start();
            }

            @Override
            public void showError(String error) {

            }
        });
    }
}
```
运行没问题。


### 2.混淆

在musicdot包的build.gradle中添加

```
task makeJar(type: proguard.gradle.ProGuardTask, dependsOn: "build") {
    // 未混淆的jar路径
    injars 'build/intermediates/bundles/release/classes.jar'
    // 混淆后的jar输出路径
    outjars 'build/outputs/music-dot-1.1.0.jar'
    // 混淆协议
    configuration 'proguard-rules.pro'
}
```


当然混淆规则proguard-rules.pro如下：

常规：
```
#表示混淆时不使用大小写混合类名
-dontusemixedcaseclassnames
#表示不跳过library中的非public的类
-dontskipnonpubliclibraryclasses
#打印混淆的详细信息
-verbose

# Optimization is turned off by default. Dex does not like code run
# through the ProGuard optimize and preverify steps (and performs some
# of these optimizations on its own).
-dontoptimize
##表示不进行校验,这个校验作用 在java平台上的
-dontpreverify
# Note that if you want to enable optimization, you cannot just
# include optimization flags in your own project configuration file;
# instead you will need to point to the
# "proguard-android-optimize.txt" file instead of this one from your
# project.properties file.

-keepattributes *Annotation*
-keep public class com.google.vending.licensing.ILicensingService
-keep public class com.android.vending.licensing.ILicensingService

# For native methods, see http://proguard.sourceforge.net/manual/examples.html#native
-keepclasseswithmembernames class * {
    native <methods>;
}

# keep setters in Views so that animations can still work.
# see http://proguard.sourceforge.net/manual/examples.html#beans
-keepclassmembers public class * extends android.view.View {
   void set*(***);
   *** get*();
}

# We want to keep methods in Activity that could be used in the XML attribute onClick
-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}

# For enumeration classes, see http://proguard.sourceforge.net/manual/examples.html#enumerations
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keepclassmembers class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator CREATOR;
}

-keepclassmembers class **.R$* {
    public static <fields>;
}

# The support library contains references to newer platform versions.
# Don't warn about those in case this app is linking against an older
# platform version.  We know about them, and they are safe.
-dontwarn android.support.**

# Understand the @Keep support annotation.
-keep class android.support.annotation.Keep

-keep @android.support.annotation.Keep class * {*;}

-keepclasseswithmembers class * {
    @android.support.annotation.Keep <methods>;
}

-keepclasseswithmembers class * {
    @android.support.annotation.Keep <fields>;
}

-keepclasseswithmembers class * {
    @android.support.annotation.Keep <init>(...);
}
#忽略警告
-ignorewarnings
#保证是独立的jar,没有任何项目引用,如果不写就会认为我们所有的代码是无用的,从而把所有的代码压缩掉,导出一个空的jar
-dontshrink
#保护泛型
-keepattributes Signature
```

本项目特殊：

```
-keep class com.gg.musicdot.PlayMusicPresenter{*;}
-keep class com.gg.musicdot.UUView{*;}
-keep class com.gg.musicdot.PlayMusicModle$* {*;}
```
此处须注意两点：
PlayMusicPresenter对外接口，不可混淆；PlayMusicModle含有匿名内部类，不可混淆。

没毛病。

-------------------

### 3.打包


在terminal窗口中写入：gradlew makejar

回车

![这里写图片描述](http://img.blog.csdn.net/20180202103239160?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvemhvdXAzMjQ=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)


打包完成。

在输出目录中找到 'build/outputs/music-dot-1.1.0.jar'

![这里写图片描述](http://img.blog.csdn.net/20180202103425578?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvemhvdXAzMjQ=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

###4.使用

最后我们用在主项目中验证可行性：

![这里写图片描述](http://img.blog.csdn.net/20180202103613148?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvemhvdXAzMjQ=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

完美。


###5.结尾

在人间有谁活着不像是一场炼狱。

