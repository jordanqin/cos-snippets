import XCTest
import QCloudCOSXML

class PutObjectSSE: XCTestCase,QCloudSignatureProvider,QCloudCredentailFenceQueueDelegate{

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

    func fenceQueue(_ queue: QCloudCredentailFenceQueue!, requestCreatorWithContinue continueBlock: QCloudCredentailFenceQueueContinue!) {
        let cre = QCloudCredential.init();
        //在这里可以同步过程从服务器获取临时签名需要的 secretID，secretKey，expiretionDate 和 token 参数
        cre.secretID = "COS_SECRETID";
        cre.secretKey = "COS_SECRETKEY";
        cre.token = "COS_TOKEN";
        /*强烈建议返回服务器时间作为签名的开始时间，用来避免由于用户手机本地时间偏差过大导致的签名不正确 */
        cre.startDate = DateFormatter().date(from: "startTime"); // 单位是秒
        cre.experationDate = DateFormatter().date(from: "expiredTime");
        let auth = QCloudAuthentationV5Creator.init(credential: cre);
        continueBlock(auth,nil);
    }

    func signature(with fileds: QCloudSignatureFields!, request: QCloudBizHTTPRequest!, urlRequest urlRequst: NSMutableURLRequest!, compelete continueBlock: QCloudHTTPAuthentationContinueBlock!) {
        self.credentialFenceQueue?.performAction({ (creator, error) in
            if error != nil {
                continueBlock(nil,error!);
            }else{
                let signature = creator?.signature(forData: urlRequst);
                continueBlock(signature,nil);
            }
        })
    }


    // 使用 COS 托管加密密钥的服务端加密（SSE-COS）保护数据
    func putObjectSse() {
        let request = QCloudCOSXMLUploadObjectRequest<AnyObject>.init();
        
        //.cssg-snippet-body-start:[swift-put-object-sse]
        request.setCOSServerSideEncyption();
        //.cssg-snippet-body-end
    }


    // 使用客户提供的加密密钥的服务端加密 （SSE-C）保护数据
    func putObjectSseC() {
        let request = QCloudCOSXMLUploadObjectRequest<AnyObject>.init();
        //.cssg-snippet-body-start:[swift-put-object-sse-c]
        let customKey = "123456qwertyuioplkjhgfdsazxcvbnm";
        request.setCOSServerSideEncyptionWithCustomerKey(customKey);
        //.cssg-snippet-body-end
    }


    // 使用 KMS 托管加密密钥的服务端加密（SSE-KMS）保护数据
    func putObjectSseKms() {
        //.cssg-snippet-body-start:[swift-put-object-sse-kms]
        
        //.cssg-snippet-body-end
    }


    // .cssg-methods-pragma

    func testPutObjectSSE() {
        // 使用 COS 托管加密密钥的服务端加密（SSE-COS）保护数据
        self.putObjectSse();
        // 使用客户提供的加密密钥的服务端加密 （SSE-C）保护数据
        self.putObjectSseC();

        // 使用 KMS 托管加密密钥的服务端加密（SSE-KMS）保护数据
        self.putObjectSseKms();
        // .cssg-methods-pragma
    }
}
