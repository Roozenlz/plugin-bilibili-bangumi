package top.roozen.bangumi.client;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import top.roozen.bangumi.request.BilibiliBangumiRequest;

/**
 * B站番剧客户端实现类
 * 用于调用B站API获取用户关注的番剧列表信息
 *
 * @author <a href="https://roozen.top">Roozen</a>
 * @version 1.0
 * @since 2023/7/30
 */
@Component
public class BilibiliBangumiClient implements IBilibiliBangumiClient{
    private final WebClient webClient = WebClient.builder().build();
    /**
     * B站番剧API地址模板
     * 包含类型编号、关注状态、用户ID、每页数量、页码等参数占位符
     */
    private static final String BILI_API_URL = "https://api.bilibili.com/x/space/bangumi/follow/list?type={typeNum}&follow_status={status}&vmid={vmid}&ps={ps}&pn={pn}";
    
    /**
     * 分页获取B站番剧数据
     * 根据请求参数调用B站API获取指定用户的番剧关注列表，并按分页返回
     *
     * @param request 包含查询条件的番剧请求对象，包括类型编号、关注状态、用户ID、每页数量、页码等参数
     * @return Flux流式响应对象，包含番剧数据的JSON节点
     */
    @Override
    public Flux<ObjectNode> listBiliBangumiByPage(BilibiliBangumiRequest request) {
        return webClient.get()
            .uri(BILI_API_URL,request.getTypeNum(),request.getStatus(),request.getVmid(),request.getPs(),request.getPn())
            // .headers(headers -> {
            // })
            .retrieve()
            .bodyToFlux(ObjectNode.class)
            .map(item->{
                return item.withObject("/data");
            });
    }
}
