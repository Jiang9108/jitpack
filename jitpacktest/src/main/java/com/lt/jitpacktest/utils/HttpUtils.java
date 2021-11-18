package com.lt.jitpacktest.utils;

import android.graphics.Bitmap;
import android.os.Handler;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpUtils {

    //线程池
    private static ExecutorService executor;
    private static Handler mHandler;

    static {
        executor = Executors.newFixedThreadPool(5);
        mHandler = new Handler();
    }

    //get
  /*  public static String doFileHttpReqeust(final String uploadUrl, final Map<String, File> files, final StringCallback callback) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection conn = null;
                OutputStream outputStream = null;
                try {
                    String BOUNDARY = java.util.UUID.randomUUID().toString();   //利用系统工具类生成界限符
                    String PREFIX = "--", LINEND = "\r\n";
                    String MULTIPART_FROM_DATA = "multipart/form-data";
                    String CHARSET = "UTF-8";

                    URL uri = new URL(uploadUrl);
                    conn = (HttpURLConnection) uri.openConnection();
                    conn.setReadTimeout(5 * 1000); // 缓存的最长时间
                    conn.setDoInput(true);// 允许输入
                    conn.setDoOutput(true);// 允许输出
                    conn.setUseCaches(false); // 不允许使用缓存
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("connection", "keep-alive");
                    conn.setRequestProperty("Charsert", "UTF-8");
                    conn.setRequestProperty("Content-Type", MULTIPART_FROM_DATA + ";boundary=" + BOUNDARY);

                    //        // 首先组拼文本类型的参数
                    //        StringBuilder sb = new StringBuilder();
                    //        for (Map.Entry<String, String> entry : params.entrySet())
                    //        {
                    //            sb.append(PREFIX);
                    //            sb.append(BOUNDARY);
                    //            sb.append(LINEND);
                    //            sb.append("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"" + LINEND);
                    //            sb.append("Content-Type: text/plain; charset=" + CHARSET + LINEND);
                    //            sb.append("Content-Transfer-Encoding: 8bit" + LINEND);
                    //            sb.append(LINEND);
                    //            sb.append(entry.getValue());
                    //            sb.append(LINEND);
                    //        }

                    DataOutputStream outStream = new DataOutputStream(conn.getOutputStream());
                    //        outStream.write(sb.toString().getBytes());
                    InputStream in = null;
                    // 发送文件数据
                    if (files != null) {
                        for (Map.Entry<String, File> file : files.entrySet()) {
                            StringBuilder sb1 = new StringBuilder();
                            sb1.append(PREFIX);
                            sb1.append(BOUNDARY);
                            sb1.append(LINEND);
                            // name是post中传参的键 filename是文件的名称
                            sb1.append("Content-Disposition: form-data; name=\"file\"; filename=\"" + file.getValue().getName() + "\"" + LINEND);
                            sb1.append("Content-Type: application/octet-stream; charset=" + CHARSET + LINEND);
                            sb1.append("Content-Transfer-Encoding: binary" + LINEND);
                            sb1.append(LINEND);
                            outStream.write(sb1.toString().getBytes());
                            Log.d("file", sb1.toString());
                            InputStream is = new FileInputStream(file.getValue());
                            byte[] buffer = new byte[1024];
                            int len = 0;
                            while ((len = is.read(buffer)) != -1) {
                                outStream.write(buffer, 0, len);
                            }

                            is.close();
                            outStream.write(LINEND.getBytes());
                        }

                        // 请求结束标志
                        byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINEND).getBytes();
                        outStream.write(end_data);
                        outStream.flush();
                        // 得到响应码
                        int res = conn.getResponseCode();
                        if (res == 200) {
                            in = conn.getInputStream();
                            int ch;
                            StringBuilder sb2 = new StringBuilder();
                            while ((ch = in.read()) != -1) {
                                sb2.append((char) ch);
                            }
                            Log.d("---fileupload---", "状态码：" + res);

                            if (sb2 != null && callback != null) {
                                postSuccessString(callback, sb2.toString());
                            }
                        } else {
                            Log.d("----fileupload---", "状态码：" + res);
                            if (callback != null) {
                                postFailed(callback, res, new Exception("请求数据失败：" + res));
                            }
                        }
                        outStream.close();
                        conn.disconnect();
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                    if (callback != null) {
                        postFailed(callback, 0, e);
                    }
                } finally {
                    if (conn != null) {
                        // 结束后，关闭连接
                        conn.disconnect();
                    }

                }
            }
        });
        return null;
    }*/

    //get
    public static String dogetHttpReqeust(final String url,
                                          final Map<String, String> params, final StringCallback callback) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                OutputStream outputStream = null;
                String geturl = null;
                if (params != null) {
                    StringBuilder sb = new StringBuilder();
                    Set<Map.Entry<String, String>> sets = params.entrySet();
                    // 将Hashmap转换为string
                    for (Map.Entry<String, String> entry : sets) {
                        sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
                    }
                    String param = sb.substring(0, sb.length() - 1);
                    geturl = url + param;
                }
                try {
                    // 调用URL对象的openConnection方法获取HttpURLConnection的实例
                    URL url = new URL(geturl);
                    connection = (HttpURLConnection) url.openConnection();
                    // 设置请求方式，GET或POST
                    connection.setRequestMethod("GET");
                    // 设置连接超时、读取超时的时间，单位为毫秒（ms）
                    connection.setConnectTimeout(60 * 1000);
                    connection.setReadTimeout(60 * 1000);

                    int responseCode = connection.getResponseCode();
                    if (responseCode == 200) {
                        InputStream inputStream = connection.getInputStream();
                        String result = inputStream2String(inputStream);
                        if (result != null && callback != null) {
                            postSuccessString(callback, result);
                        }
                    } else {
                        if (callback != null) {
                            postFailed(callback, responseCode, new Exception("请求数据失败：" + responseCode));
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    if (callback != null) {
                        postFailed(callback, 0, e);
                    }
                } finally {
                    if (connection != null) {
                        // 结束后，关闭连接
                        connection.disconnect();
                    }
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
        });
        return null;
    }

    /**
     * 执行网络请求操作,返回数据会解析成字符串String
     *
     * @param method 请求方式(需要传入String类型的参数:,"POST")
     * @param url    请求的url
     * @param params 请求的参数
     */
    public static String doHttpReqeust(final String method, final String url,
                                       final Map<String, String> params, final StringCallback callback) {

        executor.execute(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                OutputStream outputStream = null;
                try {
                    URL u = new URL(url);
                    connection = (HttpURLConnection) u.openConnection();
                    // 设置输入可用
                    connection.setDoInput(true);
                    // 设置输出可用
                    connection.setDoOutput(true);

                    // 设置请求方式
                    connection.setRequestMethod(method);
                    // 设置连接超时
                    connection.setConnectTimeout(100000);
                    // 设置读取超时
                    connection.setReadTimeout(100000);
                    // 设置缓存不可用
                    connection.setUseCaches(false);
                    // 开始连接
                    connection.connect();


                    // 只有当POST请求时才会执行此代码段
                    if (params != null) {
                        // 获取输出流,connection.getOutputStream已经包含了connect方法的调用
                        outputStream = connection.getOutputStream();
                        StringBuilder sb = new StringBuilder();
                        Set<Map.Entry<String, String>> sets = params.entrySet();
                        // 将Hashmap转换为string
                        for (Map.Entry<String, String> entry : sets) {
                            sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
                        }
                        String param = sb.substring(0, sb.length() - 1);
                        String requestUrl = url + param;
                        // 使用输出流将string类型的参数写到服务器
                        outputStream.write(param.getBytes());
                        outputStream.flush();
                    }
                    int responseCode = connection.getResponseCode();
                    if (responseCode == 200) {
                        InputStream inputStream = connection.getInputStream();
                        String result = inputStream2String(inputStream);
                        if (result != null && callback != null) {
                            postSuccessString(callback, result);
                        }
                    } else {
                        if (callback != null) {
                            postFailed(callback, responseCode, new Exception("请求数据失败：" + responseCode));
                        }
                    }

                } catch (final Exception e) {
                    e.printStackTrace();
                    if (callback != null) {
                        postFailed(callback, 0, e);
                    }

                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        return null;
    }

//    /**
//     * 执行网络请求操作,返回数据是Bitmap
//     *
//     * @param method 请求方式(需要传入String类型的参数:"GET","POST")
//     * @param url    请求的url
//     * @param params 请求的参数
//     */
/*    public static String doHttpReqeust(final String method, final String url,
                                       final Map<String, String> params, final BitmapCallback callback) {

        executor.execute(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                OutputStream outputStream = null;
                InputStream inputStream = null;
                try {
                    URL u = new URL(url);
                    connection = (HttpURLConnection) u.openConnection();
                    // 设置输入可用
                    connection.setDoInput(true);
                    // 设置输出可用
                    connection.setDoOutput(true);
                    // 设置请求方式
                    connection.setRequestMethod(method);
                    // 设置连接超时
                    connection.setConnectTimeout(5000);
                    // 设置读取超时
                    connection.setReadTimeout(5000);
                    // 设置缓存不可用
                    connection.setUseCaches(false);
                    // 开始连接
                    connection.connect();

                    // 只有当POST请求时才会执行此代码段
                    if (params != null) {
                        // 获取输出流,connection.getOutputStream已经包含了connect方法的调用
                        outputStream = connection.getOutputStream();
                        StringBuilder sb = new StringBuilder();
                        Set<Map.Entry<String, String>> sets = params.entrySet();
                        // 将Hashmap转换为string
                        for (Map.Entry<String, String> entry : sets) {
                            sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
                        }
                        String param = sb.substring(0, sb.length() - 1);
                        // 使用输出流将string类型的参数写到服务器
                        outputStream.write(param.getBytes());
                        outputStream.flush();
                    }

                    int responseCode = connection.getResponseCode();
                    if (responseCode == 200) {
                        inputStream = connection.getInputStream();
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        if (bitmap != null && callback != null) {
                            postSuccessBitmap(callback, bitmap);
                        }
                    } else {
                        if (callback != null) {
                            postFailed(callback, responseCode, new Exception("请求图片失败：" + responseCode));
                        }
                    }


                } catch (final Exception e) {
                    e.printStackTrace();
                    if (callback != null) {
                        postFailed(callback, 0, e);
                    }

                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        return null;
    }*/

    // 分割符
    private static final String BOUNDARY = "----WebKitFormBoundaryT1HoybnYeFOGFlBR";

    /**
     * HttpUrlConnection　实现文件上传
     *
     * @param params       普通参数
     * @param fileFormName 文件在表单中的键
     * @param uploadFile   上传的文件
     * @param newFileName  文件在表单中的值（服务端获取到的文件名）
     * @param urlStr       url
     * @throws IOException
     */
    public static void uploadForm(final Map<String, String> params, final String fileFormName, final File uploadFile
            , String newFileName, final String urlStr, final StringCallback callback) {
        if (newFileName == null || newFileName.trim().equals("")) {
            newFileName = uploadFile.getName();
        }
        final StringBuilder sb = new StringBuilder();
        final String names = newFileName;

        executor.execute(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection conn = null;
                try {
                    /**
                     * 上传文件的头
                     */
                    sb.append("--" + BOUNDARY + "\r\n");
                    sb.append("Content-Disposition: form-data; name=\"" + fileFormName + "\"; filename=\"" + names + "\""
                            + "\r\n");
                    sb.append("Content-Type: image/jpeg" + "\r\n");// 如果服务器端有文件类型的校验，必须明确指定ContentType
                    sb.append("\r\n");

                    byte[] headerInfo = sb.toString().getBytes("UTF-8");
                    byte[] endInfo = ("\r\n--" + BOUNDARY + "--\r\n").getBytes("UTF-8");


                    URL url = new URL(urlStr);
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    // 设置传输内容的格式，以及长度
                    conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
                    conn.setRequestProperty("Content-Length",
                            String.valueOf(headerInfo.length + uploadFile.length() + endInfo.length));
                    conn.setDoOutput(true);

                    OutputStream out = conn.getOutputStream();
                    InputStream in = new FileInputStream(uploadFile);
                    // 写入头部 （包含了普通的参数，以及文件的标示等）
                    out.write(headerInfo);
                    // 写入文件
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = in.read(buf)) != -1) {
                        out.write(buf, 0, len);
                    }
                    // 写入尾部
                    out.write(endInfo);
                    in.close();
                    out.close();
                    int responseCode = conn.getResponseCode();
                    if (responseCode == 200) {
                        System.out.println("文件上传成功");
                        InputStream inputStream = conn.getInputStream();
                        String result = inputStream2String(inputStream);
                        if (result != null && callback != null) {
                            postSuccessString(callback, result);
                        }
                    } else {
                        if (callback != null) {
                            postFailed(callback, responseCode, new Exception("请求数据失败：" + responseCode));
                        }
                    }
                } catch (IOException e) {

                    if (callback != null) {
                        postFailed(callback, 0, e);
                    }
                } finally {
                    if (conn != null) {
                        // 结束后，关闭连接
                        conn.disconnect();
                    }

                }
            }

        });

    }


    private static void postSuccessString(final StringCallback callback, final String result) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                callback.onSuccess(result);
            }
        });
    }

    private static void postSuccessBitmap(final Callback callback, final Bitmap bitmap) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                BitmapCallback bitmapCallback = (BitmapCallback) callback;
                bitmapCallback.onSuccess(bitmap);
            }
        });
    }

    private static void postSuccessByte(final Callback callback, final byte[] bytes) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                ByteArrayCallback byteArrayCallback = (ByteArrayCallback) callback;
                byteArrayCallback.onSuccess(bytes);
            }
        });
    }

    private static <T> void postSuccessObject(final ObjectCallback callback, final T t) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                ObjectCallback objectCallback = (ObjectCallback) callback;
                objectCallback.onSuccess(t);
            }
        });
    }

    private static void postFailed(final Callback callback, final int code, final Exception e) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                callback.onFaileure(code, e);
            }
        });
    }

    /**
     * 字节流转换成字符串
     *
     * @param inputStream
     * @return
     */
    private static String inputStream2String(InputStream inputStream) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] bytes = new byte[1024];
        int len = 0;
        try {
            while ((len = inputStream.read(bytes)) != -1) {
                baos.write(bytes, 0, len);
            }
            return new String(baos.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (baos != null) {
                try {
                    baos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

//    /**
//     * 字节流转换成字节数组
//     *
//     * @param inputStream 输入流
//     * @return
//     */
/*    public static byte[] inputStream2ByteArray(InputStream inputStream) {
        byte[] result = null;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        // 缓冲区
        byte[] bytes = new byte[1024];
        int len = -1;
        try {
            // 使用字节数据输出流来保存数据
            while ((len = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, len);
            }
            result = outputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }*/

//    /**
//     * 判断是否联网
//     *
//     * @param context
//     * @return
//     */
/*
    public static boolean isNetWorkConnected(Context context) {

        ConnectivityManager manager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        if (networkInfo != null) {
            return networkInfo.isAvailable();
        }
        return false;
    }
*/


    public interface Callback {
        void onFaileure(int code, Exception e);
    }

    public interface StringCallback extends Callback {
        void onSuccess(String response);
    }

    public interface BitmapCallback extends Callback {
        void onSuccess(Bitmap bitmap);
    }

    public interface ByteArrayCallback extends Callback {
        void onSuccess(byte[] bytes);
    }

    public interface ObjectCallback<T> extends Callback {
        void onSuccess(T t);
    }
}
