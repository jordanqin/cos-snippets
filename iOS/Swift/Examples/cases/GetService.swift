import XCTest
import QCloudCOSXML

class GetService: XCTestCase,QCloudSignatureProvider,QCloudCredentailFenceQueueDelegate{

    var credentialFenceQueue:QCloudCredentailFenceQueue?;

    override func setUp() {
        let config = QCloudServiceConfiguration.init();
        config.signatureProvider = self;
        config.appID = "1253653367";
        let endpoint = QCloudCOSXMLEndPoint.init();
        endpoint.regionName = "ap-guangzhou";//服务地域名称，可用的地域请参考注释
        endpoint.useHTTPS = true;
        config.endpoint = endpoint;
        QCloudCOSXMLService.registerDefaultCOSXML(with: config);
        QCloudCOSTransferMangerService.registerDefaultCOSTransferManger(with: config);

        // 脚手架用于获取临时密钥
        self.credentialFenceQueue = QCloudCredentailFenceQueue();
        self.credentialFenceQueue?.delegate = self;
    }

    func fenceQueue(_ queue: QCloudCredentailFenceQueue!,
                    requestCreatorWithContinue continueBlock: QCloudCredentailFenceQueueContinue!) {
        let cre = QCloudCredential.init();
        // 在这里可以同步过程从服务器获取临时签名需要的 secretID，secretKey，expiretionDate 和 token 参数
        cre.secretID = "COS_SECRETID";
        cre.secretKey = "COS_SECRETKEY";
        cre.token = "COS_TOKEN";
        /*强烈建议返回服务器时间作为签名的开始时间，用来避免由于用户手机本地时间偏差过大导致的签名不正确 */
        cre.startDate = DateFormatter().date(from: "startTime"); // 单位是秒
        cre.expirationDate = DateFormatter().date(from: "expiredTime");
        let auth = QCloudAuthentationV5Creator.init(credential: cre);
        continueBlock(auth,nil);
    }

    func signature(with fileds: QCloudSignatureFields!,
                   request: QCloudBizHTTPRequest!,
                   urlRequest urlRequst: NSMutableURLRequest!,
                   compelete continueBlock: QCloudHTTPAuthentationContinueBlock!) {
        self.credentialFenceQueue?.performAction({ (creator, error) in
            if error != nil {
                continueBlock(nil,error!);
            }else{
                let signature = creator?.signature(forData: urlRequst);
                continueBlock(signature,nil);
            }
        })
    }


    // 获取存储桶列表
    func getService() {
        //.cssg-snippet-body-start:[swift-get-service]
        
        // 获取所属账户的所有存储空间列表的方法.
        let getServiceReq = QCloudGetServiceRequest.init();
        getServiceReq.setFinish{(result,error) in
            if let result = result {
                let buckets = result.buckets
                let owner = result.owner
            } else {
                print(error!);
            }
        }
        QCloudCOSXMLService.defaultCOSXML().getService(getServiceReq);
        
        //.cssg-snippet-body-end
    }


    // 获取地域的存储桶列表
    func getRegionalService() {
      
        //不支持
        //.cssg-snippet-body-start:[swift-get-regional-service]
        //.cssg-snippet-body-end     
    }
    // 计算签名
    func getAuthorization() {
    
        //.cssg-snippet-body-start:[swift-get-authorization]
        
        //.cssg-snippet-body-end
    }
    // .cssg-methods-pragma

    func testGetService() {
        // 获取存储桶列表
        self.getService();
        // 获取地域的存储桶列表
        self.getRegionalService();
        // 计算签名
        self.getAuthorization();
        // .cssg-methods-pragma
    }
}
