using COSXML.Common;
using COSXML.CosException;
using COSXML.Model;
using COSXML.Model.Object;
using COSXML.Model.Tag;
using COSXML.Model.Bucket;
using COSXML.Model.Service;
using COSXML.Utils;
using COSXML.Auth;
using COSXML.Transfer;
using System;
using COSXML;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Reflection;
using System.Text;
using System.Threading;
using System.Threading.Tasks;

namespace COSSnippet
{
    public class BucketACLModel {

      private CosXml cosXml;

      BucketACLModel() {
        CosXmlConfig config = new CosXmlConfig.Builder()
          .SetRegion("COS_REGION") // 设置默认的区域, COS 地域的简称请参照 https://cloud.tencent.com/document/product/436/6224
          .Build();
        
        string secretId = "SECRET_ID";   // 云 API 密钥 SecretId, 获取 API 密钥请参照 https://console.cloud.tencent.com/cam/capi
        string secretKey = "SECRET_KEY"; // 云 API 密钥 SecretKey, 获取 API 密钥请参照 https://console.cloud.tencent.com/cam/capi
        long durationSecond = 600;          //每次请求签名有效时长，单位为秒
        QCloudCredentialProvider qCloudCredentialProvider = new DefaultQCloudCredentialProvider(secretId, 
          secretKey, durationSecond);
        
        this.cosXml = new CosXmlServer(config, qCloudCredentialProvider);
      }

      /// 设置存储桶 ACL
      public void PutBucketAcl()
      {
        //.cssg-snippet-body-start:[put-bucket-acl]
        try
        {
          // 存储桶名称，此处填入格式必须为 bucketname-APPID, 其中 APPID 获取参考 https://console.cloud.tencent.com/developer
          string bucket = "examplebucket-1250000000";
          PutBucketACLRequest request = new PutBucketACLRequest(bucket);
          //设置私有读写权限
          request.SetCosACL(CosACL.Private);
          //授予1131975903账号读权限
          COSXML.Model.Tag.GrantAccount readAccount = new COSXML.Model.Tag.GrantAccount();
          readAccount.AddGrantAccount("1131975903", "1131975903");
          request.SetXCosGrantRead(readAccount);
          //执行请求
          PutBucketACLResult result = cosXml.PutBucketACL(request);
          //请求成功
          Console.WriteLine(result.GetResultInfo());
        }
        catch (COSXML.CosException.CosClientException clientEx)
        {
          //请求失败
          Console.WriteLine("CosClientException: " + clientEx);
        }
        catch (COSXML.CosException.CosServerException serverEx)
        {
          //请求失败
          Console.WriteLine("CosServerException: " + serverEx.GetInfo());
        }
        
        //.cssg-snippet-body-end
      }

      /// 获取存储桶 ACL
      public void GetBucketAcl()
      {
        //.cssg-snippet-body-start:[get-bucket-acl]
        try
        {
          // 存储桶名称，此处填入格式必须为 bucketname-APPID, 其中 APPID 获取参考 https://console.cloud.tencent.com/developer
          string bucket = "examplebucket-1250000000";
          GetBucketACLRequest request = new GetBucketACLRequest(bucket);
          //执行请求
          GetBucketACLResult result = cosXml.GetBucketACL(request);
          //存储桶的 ACL 信息
          AccessControlPolicy acl = result.accessControlPolicy;
        }
        catch (COSXML.CosException.CosClientException clientEx)
        {
          //请求失败
          Console.WriteLine("CosClientException: " + clientEx);
        }
        catch (COSXML.CosException.CosServerException serverEx)
        {
          //请求失败
          Console.WriteLine("CosServerException: " + serverEx.GetInfo());
        }
        
        //.cssg-snippet-body-end
      }

      // .cssg-methods-pragma

      static void Main(string[] args)
      {
        BucketACLModel m = new BucketACLModel();

        /// 设置存储桶 ACL
        m.PutBucketAcl();
        /// 获取存储桶 ACL
        m.GetBucketAcl();
        // .cssg-methods-pragma
      }
    }
}
