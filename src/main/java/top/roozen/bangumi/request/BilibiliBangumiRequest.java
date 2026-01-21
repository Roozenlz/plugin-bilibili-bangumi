package top.roozen.bangumi.request;

import lombok.Builder;
import lombok.Data;

/**
 * B站番剧请求实体类
 * 用于封装B站番剧相关API的请求参数
 *
 * @author <a href="https://roozen.top">Roozen</a>
 * @version 1.0
 * @since 2023/7/30
 */
@Data
@Builder
public class BilibiliBangumiRequest {

    private String vmid;
    
    /**
     * 观看状态
     * 1.想看
     * 2.在看
     * 3.已看   
     */
    private int status;

    private boolean useWebp;
    
    // 追番类型：1.追番 2.追剧
    private int typeNum;
    
    // 每页大小，默认为30
    @Builder.Default()
    private int ps = 30;
    
    // 页码
    private int pn;
}