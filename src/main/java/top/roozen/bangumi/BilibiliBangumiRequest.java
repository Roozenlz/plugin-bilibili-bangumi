package top.roozen.bangumi;

import lombok.Builder;
import lombok.Data;

/**
 * @author <a href="https://roozen.top">Roozen</a>
 * @version 1.0
 * @since 2023/7/30
 */
@Data
@Builder
public class BilibiliBangumiRequest {

    private String vmid;
    /**
     * 1.wantWatch
     * 2.watching
     * 3.watched
     */
    private int status;

    private boolean useWebp;

    private int typeNum;
    //pageSize
    @Builder.Default()
    private int ps = 30;
    //pageNum
    private int pn;
}
