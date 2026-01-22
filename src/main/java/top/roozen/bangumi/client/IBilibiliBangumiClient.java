package top.roozen.bangumi.client;

import com.fasterxml.jackson.databind.node.ObjectNode;
import reactor.core.publisher.Flux;
import top.roozen.bangumi.request.BilibiliBangumiRequest;

/**
 * Bilibili番剧客户端接口
 * 提供Bilibili番剧相关数据的访问功能
 *
 * @author <a href="https://roozen.top">Roozen</a>
 * @version 1.0
 * @since 2023/7/30
 */
public interface IBilibiliBangumiClient {
    /**
     * 分页获取Bilibili番剧列表
     * 根据请求参数分页查询Bilibili番剧信息
     *
     * @param request 请求参数对象，包含分页和其他查询条件
     * @return Flux流式响应，每个元素为包含番剧信息的ObjectNode对象
     */
    Flux<ObjectNode> listBiliBangumiByPage(BilibiliBangumiRequest request);
}
