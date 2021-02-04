package com.mango.oss.aliyun_oss.controller;


import com.aliyun.oss.OSSClient;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.model.MatchMode;
import com.aliyun.oss.model.PolicyConditions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

@Controller
public class AliyunOssController {

    @Value("${oss.accessId}")
    private String accessId; // 请填写您的AccessKeyId。
    @Value("${oss.accessKey}")
    private String accessKey; // 请填写您的AccessKeySecret。
    @Value("${oss.endpoint}")
    private String endpoint; // 请填写您的 endpoint。
    @Value("${oss.bucket}")
    private String bucket; // 请填写您的 bucketname 。

    /**
     * 获取服务端签名参数
     * @return
     */
    @ResponseBody
    @GetMapping("/aliyun/oss/getpolicy")
    public Map<String, String> getPolicy(){

        System.out.println("123");
        String host = "http://" + bucket + "." + endpoint; // host的格式为 bucketname.endpoint
        String dir = new SimpleDateFormat("yyyy-MM-dd").format(new Date()); // 用户上传文件时指定的前缀。
        OSSClient client = new OSSClient(endpoint, accessId, accessKey);

        Map<String, String> respMap = null;
        try {
            long expireTime = 30;
            long expireEndTime = System.currentTimeMillis() + expireTime * 1000;
            Date expiration = new Date(expireEndTime);
            PolicyConditions policyConds = new PolicyConditions();
            policyConds.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE, 0, 1048576000);
            policyConds.addConditionItem(MatchMode.StartWith, PolicyConditions.COND_KEY, dir);

            String postPolicy = client.generatePostPolicy(expiration, policyConds);
            byte[] binaryData = postPolicy.getBytes("utf-8");
            String encodedPolicy = BinaryUtil.toBase64String(binaryData);
            String postSignature = client.calculatePostSignature(postPolicy);

            respMap = new LinkedHashMap<String, String>();
            respMap.put("accessid", accessId);
            respMap.put("policy", encodedPolicy);
            respMap.put("signature", postSignature);
            respMap.put("dir", dir);
            respMap.put("host", host);
            respMap.put("expire", String.valueOf(expireEndTime / 1000));
            // respMap.put("expire", formatISO8601Date(expiration));

            /* //回调设置
            // callbackUrl为 上传回调服务器的URL，请将下面的IP和Port配置为您自己的真实信息。
            // String callbackUrl = "http://88.88.88.88.:8888";
            JSONObject jasonCallback = new JSONObject();
            jasonCallback.put("callbackUrl", callbackUrl);
            jasonCallback.put("callbackBody",
                    "filename=${object}&size=${size}&mimeType=${mimeType}&height=${imageInfo.height}&width=${imageInfo.width}");
            jasonCallback.put("callbackBodyType", "application/x-www-form-urlencoded");
            String base64CallbackBody = BinaryUtil.toBase64String(jasonCallback.toString().getBytes());
            respMap.put("callback", base64CallbackBody);

            JSONObject ja1 = JSONObject.fromObject(respMap);
            // System.out.println(ja1.toString());
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setHeader("Access-Control-Allow-Methods", "GET, POST");
            response(request, response, ja1.toString());
*/
        } catch (Exception e) {
            // Assert.fail(e.getMessage());
            System.out.println(e.getMessage());
        }

        return respMap;
    }

    /**
     * 调用页面
     * @return
     */
    @GetMapping("/getindex")
    public String index(){
        return "index";
    }
}
