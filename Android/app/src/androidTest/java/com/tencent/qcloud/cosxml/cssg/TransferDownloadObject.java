package com.tencent.qcloud.cosxml.cssg;

import android.support.annotation.Nullable;

import com.tencent.cos.xml.*;
import com.tencent.cos.xml.common.*;
import com.tencent.cos.xml.exception.*;
import com.tencent.cos.xml.listener.*;
import com.tencent.cos.xml.model.*;
import com.tencent.cos.xml.model.object.*;
import com.tencent.cos.xml.model.bucket.*;
import com.tencent.cos.xml.model.tag.*;
import com.tencent.cos.xml.transfer.*;
import com.tencent.qcloud.core.auth.*;
import com.tencent.qcloud.core.common.*;
import com.tencent.qcloud.core.http.*;
import com.tencent.cos.xml.model.service.*;


import android.content.Context;
import android.util.Log;
import android.support.test.InstrumentationRegistry;

import org.junit.Test;

import java.net.*;
import java.util.*;
import java.nio.charset.Charset;
import java.io.*;

public class TransferDownloadObject {

    private Context context;
    private CosXmlService cosXmlService;

    public static class ServerCredentialProvider extends BasicLifecycleCredentialProvider {
        
        @Override
        protected QCloudLifecycleCredentials fetchNewCredentials() throws QCloudClientException {
    
            // 首先从您的临时密钥服务器获取包含了密钥信息的响应
			// 临时密钥生成和使用指引参见https://cloud.tencent.com/document/product/436/14048
    
            // 然后解析响应，获取密钥信息
            String tmpSecretId = "临时密钥 secretId";
            String tmpSecretKey = "临时密钥 secretKey";
            String sessionToken = "临时密钥 TOKEN";
            long expiredTime = 1556183496L;//临时密钥有效截止时间戳，单位是秒
    
            /*强烈建议返回服务器时间作为签名的开始时间，用来避免由于用户手机本地时间偏差过大导致的签名不正确 */
            // 返回服务器时间作为签名的起始时间
            long startTime = 1556182000L; //临时密钥有效起始时间，单位是秒
    
            // 最后返回临时密钥信息对象
            return new SessionQCloudCredentials(tmpSecretId, tmpSecretKey, sessionToken, startTime, expiredTime);
        }
    }

    /**
     * 高级接口下载对象
     */
    private void transferDownloadObject() {
        //.cssg-snippet-body-start:[transfer-download-object]
        // 高级下载接口支持断点续传，所以会在下载前先发起 HEAD 请求获取文件信息。
        // 如果您使用的是临时密钥或者使用子账号访问，请确保权限列表中包含 HeadObject 的权限。

        // 初始化 TransferConfig，这里使用默认配置，如果需要定制，请参考 SDK 接口文档
        TransferConfig transferConfig = new TransferConfig.Builder().build();
        //初始化 TransferManager
        TransferManager transferManager = new TransferManager(cosXmlService,
                transferConfig);

        // 存储桶名称，由bucketname-appid 组成，appid必须填入，可以在COS控制台查看存储桶名称。 https://console.cloud.tencent.com/cos5/bucket
		String bucket = "examplebucket-1250000000";
        String cosPath = "exampleobject"; //对象在存储桶中的位置标识符，即称对象键
        //本地目录路径
        String savePathDir = context.getExternalCacheDir().toString();
        //本地保存的文件名，若不填（null），则与 COS 上的文件名一样
        String savedFileName = "exampleobject";

        Context applicationContext = context.getApplicationContext(); // application
        // context
        COSXMLDownloadTask cosxmlDownloadTask =
                transferManager.download(applicationContext,
                        bucket, cosPath, savePathDir, savedFileName);

        //设置下载进度回调
        cosxmlDownloadTask.setCosXmlProgressListener(new CosXmlProgressListener() {
            @Override
            public void onProgress(long complete, long target) {
                // todo Do something to update progress...
            }
        });
        //设置返回结果回调
        cosxmlDownloadTask.setCosXmlResultListener(new CosXmlResultListener() {
            @Override
            public void onSuccess(CosXmlRequest request, CosXmlResult result) {
                COSXMLDownloadTask.COSXMLDownloadTaskResult downloadTaskResult =
                        (COSXMLDownloadTask.COSXMLDownloadTaskResult) result;
            }

            // 如果您使用 kotlin 语言来调用，请注意回调方法中的异常是可空的，否则不会回调 onFail 方法，即：
            // clientException 的类型为 CosXmlClientException?，serviceException 的类型为 CosXmlServiceException?
            @Override
            public void onFail(CosXmlRequest request,
                               @Nullable CosXmlClientException clientException,
                               @Nullable CosXmlServiceException serviceException) {
                if (clientException != null) {
                    clientException.printStackTrace();
                } else {
                    serviceException.printStackTrace();
                }
            }
        });
        //设置任务状态回调，可以查看任务过程
        cosxmlDownloadTask.setTransferStateListener(new TransferStateListener() {
            @Override
            public void onStateChanged(TransferState state) {
                // todo notify transfer state
            }
        });
        //.cssg-snippet-body-end
    }

    /**
     * 下载暂停、续传与取消
     */
    private void transferDownloadObjectInteract() {

        // 初始化 TransferConfig，这里使用默认配置，如果需要定制，请参考 SDK 接口文档
        TransferConfig transferConfig = new TransferConfig.Builder().build();
        //初始化 TransferManager
        TransferManager transferManager = new TransferManager(cosXmlService,
                transferConfig);

        // 存储桶名称，由bucketname-appid 组成，appid必须填入，可以在COS控制台查看存储桶名称。 https://console.cloud.tencent.com/cos5/bucket
		String bucket = "examplebucket-1250000000";
        String cosPath = "exampleobject"; //对象在存储桶中的位置标识符，即称对象键
        //本地目录路径
        String savePathDir = context.getExternalCacheDir().toString();
        //本地保存的文件名，若不填（null），则与 COS 上的文件名一样
        String savedFileName = "exampleobject";

        Context applicationContext = context.getApplicationContext(); // application
        // context
        COSXMLDownloadTask cosxmlDownloadTask =
                transferManager.download(applicationContext,
                        bucket, cosPath, savePathDir, savedFileName);

        //.cssg-snippet-body-start:[transfer-download-object-pause]
        cosxmlDownloadTask.pause();;
        //.cssg-snippet-body-end

        //.cssg-snippet-body-start:[transfer-download-object-resume]
        cosxmlDownloadTask.resume();
        //.cssg-snippet-body-end

        //.cssg-snippet-body-start:[transfer-download-object-cancel]
        cosxmlDownloadTask.cancel();
        //.cssg-snippet-body-end
    }

    /**
     * 批量下载
     */
    private void transferBatchDownloadObjects() {

        // 高级下载接口支持断点续传，所以会在下载前先发起 HEAD 请求获取文件信息。
        // 如果您使用的是临时密钥或者使用子账号访问，请确保权限列表中包含 HeadObject 的权限。

        // 初始化 TransferConfig，这里使用默认配置，如果需要定制，请参考 SDK 接口文档
        TransferConfig transferConfig = new TransferConfig.Builder().build();
        //初始化 TransferManager
        TransferManager transferManager = new TransferManager(cosXmlService,
                transferConfig);

        // 存储桶名称，由bucketname-appid 组成，appid必须填入，可以在COS控制台查看存储桶名称。 https://console.cloud.tencent.com/cos5/bucket
		String bucket = "examplebucket-1250000000";
        //本地目录路径
        String savePathDir = context.getExternalCacheDir().toString();
        //本地保存的文件名，若不填（null），则与 COS 上的文件名一样
        String savedFileName = "exampleobject";

        Context applicationContext = context.getApplicationContext(); // application

        //.cssg-snippet-body-start:[transfer-batch-download-objects]
        // 对象在存储桶中的位置标识符，即称对象键
        String[] cosPaths = new String[] {
                "exampleobject1",
                "exampleobject2",
                "exampleobject3",
        };

        for (String cosPath : cosPaths) {

            COSXMLDownloadTask cosxmlDownloadTask =
                    transferManager.download(applicationContext,
                            bucket, cosPath, savePathDir, savedFileName);
            // 设置返回结果回调
            cosxmlDownloadTask.setCosXmlResultListener(new CosXmlResultListener() {
                @Override
                public void onSuccess(CosXmlRequest request, CosXmlResult result) {
                    COSXMLDownloadTask.COSXMLDownloadTaskResult downloadResult =
                            (COSXMLDownloadTask.COSXMLDownloadTaskResult) result;
                }

            // 如果您使用 kotlin 语言来调用，请注意回调方法中的异常是可空的，否则不会回调 onFail 方法，即：
            // clientException 的类型为 CosXmlClientException?，serviceException 的类型为 CosXmlServiceException?
            @Override
            public void onFail(CosXmlRequest request,
                               @Nullable CosXmlClientException clientException,
                               @Nullable CosXmlServiceException serviceException) {
                    if (clientException != null) {
                        clientException.printStackTrace();
                    } else {
                        serviceException.printStackTrace();
                    }
                }
            });
        }
        //.cssg-snippet-body-end
    }

    /**
     * 下载文件夹
     */
    private void transferDownloadDirectory() {

        // 高级下载接口支持断点续传，所以会在下载前先发起 HEAD 请求获取文件信息。
        // 如果您使用的是临时密钥或者使用子账号访问，请确保权限列表中包含 HeadObject 的权限。

        // 初始化 TransferConfig，这里使用默认配置，如果需要定制，请参考 SDK 接口文档
        TransferConfig transferConfig = new TransferConfig.Builder().build();
        //初始化 TransferManager
        TransferManager transferManager = new TransferManager(cosXmlService,
                transferConfig);
        // 存储桶region可以在COS控制台指定存储桶的概览页查看 https://console.cloud.tencent.com/cos5/bucket/ ，关于地域的详情见 https://cloud.tencent.com/document/product/436/6224
        String region = "COS_REGION";
        // 存储桶名称，由bucketname-appid 组成，appid必须填入，可以在COS控制台查看存储桶名称。 https://console.cloud.tencent.com/cos5/bucket
		String bucket = "examplebucket-1250000000";
        String directoryPath = "exampledirectory/"; // 目录路径，必须以 / 结尾
        //本地目录路径
        String savePathDir = context.getExternalCacheDir().toString();

        Context applicationContext = context.getApplicationContext(); // application

        //.cssg-snippet-body-start:[transfer-download-directory]
        boolean isTruncated = true;
        String marker = null;
        try {
            while (isTruncated) {
                GetBucketRequest getBucketRequest = new GetBucketRequest(region, bucket, directoryPath);
                // 设置分页信息
                getBucketRequest.setMarker(marker);
                // 设置不查询子目录
                getBucketRequest.setDelimiter("/");
                GetBucketResult getBucketResult = cosXmlService.getBucket(getBucketRequest);
                // 批量下载
                for (ListBucket.Contents content : getBucketResult.listBucket.contentsList) {
                    GetObjectRequest getObjectRequest = new GetObjectRequest(bucket, content.key, savePathDir);
                    transferManager.download(context,getObjectRequest);
                }
                isTruncated = getBucketResult.listBucket.isTruncated;
                marker = getBucketResult.listBucket.nextMarker;
            }
        } catch (CosXmlServiceException serviceException) {
            serviceException.printStackTrace();
        } catch (CosXmlClientException clientException) {
            clientException.printStackTrace();
        }
        //.cssg-snippet-body-end
    }

    /**
     * 下载时对单链接限速
     */
    private void downloadObjectTrafficLimit() {
        //.cssg-snippet-body-start:[download-object-traffic-limit]
        //.cssg-snippet-body-start:[transfer-download-object]
        // 高级下载接口支持断点续传，所以会在下载前先发起 HEAD 请求获取文件信息。
        // 如果您使用的是临时密钥或者使用子账号访问，请确保权限列表中包含 HeadObject 的权限。

        // 初始化 TransferConfig，这里使用默认配置，如果需要定制，请参考 SDK 接口文档
        TransferConfig transferConfig = new TransferConfig.Builder().build();
        //初始化 TransferManager
        TransferManager transferManager = new TransferManager(cosXmlService,
                transferConfig);

        // 存储桶名称，由bucketname-appid 组成，appid必须填入，可以在COS控制台查看存储桶名称。 https://console.cloud.tencent.com/cos5/bucket
		String bucket = "examplebucket-1250000000";
        String cosPath = "exampleobject"; //对象在存储桶中的位置标识符，即称对象键
        //本地目录路径
        String savePathDir = context.getExternalCacheDir().toString();
        //本地保存的文件名，若不填（null），则与 COS 上的文件名一样
        String savedFileName = "exampleobject";

        GetObjectRequest getObjectRequest = new GetObjectRequest(bucket, cosPath, savePathDir, savedFileName);
        // 设置单链接限速，单位为 bit/s，示例设置为 1M/s
        getObjectRequest.setTrafficLimit(1024 * 1024 * 8);

        Context applicationContext = context.getApplicationContext(); // application
        // context
        COSXMLDownloadTask cosxmlDownloadTask =
                transferManager.download(applicationContext, getObjectRequest);

        //设置下载进度回调
        cosxmlDownloadTask.setCosXmlProgressListener(new CosXmlProgressListener() {
            @Override
            public void onProgress(long complete, long target) {
                // todo Do something to update progress...
            }
        });
        //设置返回结果回调
        cosxmlDownloadTask.setCosXmlResultListener(new CosXmlResultListener() {
            @Override
            public void onSuccess(CosXmlRequest request, CosXmlResult result) {
                COSXMLDownloadTask.COSXMLDownloadTaskResult downloadTaskResult =
                        (COSXMLDownloadTask.COSXMLDownloadTaskResult) result;
            }

            // 如果您使用 kotlin 语言来调用，请注意回调方法中的异常是可空的，否则不会回调 onFail 方法，即：
            // clientException 的类型为 CosXmlClientException?，serviceException 的类型为 CosXmlServiceException?
            @Override
            public void onFail(CosXmlRequest request,
                               @Nullable CosXmlClientException clientException,
                               @Nullable CosXmlServiceException serviceException) {
                if (clientException != null) {
                    clientException.printStackTrace();
                } else {
                    serviceException.printStackTrace();
                }
            }
        });
        //设置任务状态回调，可以查看任务过程
        cosxmlDownloadTask.setTransferStateListener(new TransferStateListener() {
            @Override
            public void onStateChanged(TransferState state) {
                // todo notify transfer state
            }
        });
        //.cssg-snippet-body-end
        //.cssg-snippet-body-end
    }

    /**
     * 设置支持断点下载
     */
    private void transferDownloadResumable() {
        //.cssg-snippet-body-start:[transfer-download-resumable]
        // 初始化 TransferConfig，这里使用默认配置，如果需要定制，请参考 SDK 接口文档
        // TransferManager 支持断点下载，您只需要保证 bucket、cosPath、savePathDir、savedFileName
        // 参数一致，SDK 便会从上次已经下载的位置继续下载。
        TransferConfig transferConfig = new TransferConfig.Builder().build();
        //初始化 TransferManager
        TransferManager transferManager = new TransferManager(cosXmlService,
                transferConfig);
        // 存储桶名称，由bucketname-appid 组成，appid必须填入，可以在COS控制台查看存储桶名称。 https://console.cloud.tencent.com/cos5/bucket
		String bucket = "examplebucket-1250000000";
        String cosPath = "exampleobject"; //对象在存储桶中的位置标识符，即称对象键
        //本地目录路径
        String savePathDir = context.getExternalCacheDir().toString();
        //本地保存的文件名，若不填（null），则与 COS 上的文件名一样
        String savedFileName = "exampleobject";

        GetObjectRequest getObjectRequest = new GetObjectRequest(bucket, cosPath, savePathDir, savedFileName);

        Context applicationContext = context.getApplicationContext(); // application
        // context
        COSXMLDownloadTask cosxmlDownloadTask =
                transferManager.download(applicationContext, getObjectRequest);
        //.cssg-snippet-body-end
    }




    // .cssg-methods-pragma

    private void initService() {
        // 存储桶region可以在COS控制台指定存储桶的概览页查看 https://console.cloud.tencent.com/cos5/bucket/ ，关于地域的详情见 https://cloud.tencent.com/document/product/436/6224
        String region = "ap-guangzhou";
        
        CosXmlServiceConfig serviceConfig = new CosXmlServiceConfig.Builder()
                .setRegion(region)
                .isHttps(true) // 使用 HTTPS 请求，默认为 HTTP 请求
                .builder();
        
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        cosXmlService = new CosXmlService(context, serviceConfig, new ServerCredentialProvider());
    }

    @Test
    public void testTransferDownloadObject() {
        initService();

        // 高级接口下载对象
        transferDownloadObject();
        
        // 下载暂停、续传与取消
        transferDownloadObjectInteract();
        
        // 批量下载
        transferBatchDownloadObjects();

        // 下载时对单链接限速
        downloadObjectTrafficLimit();

        // 设置支持断点下载
        transferDownloadResumable();
        
        
        
        
        // .cssg-methods-pragma
    }
}
