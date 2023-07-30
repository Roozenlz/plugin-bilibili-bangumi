package top.roozen.bangumi;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

/**
 * @author <a href="https://roozen.top">Roozen</a>
 * @version 1.0
 * @since 2023/7/30
 */
@Component
public class BilibiliBangumiClient implements IBilibiliBangumiClient{
    private final WebClient webClient = WebClient.builder().build();
    private static final String BILI_API_URL = "https://api.bilibili.com/x/space/bangumi/follow/list?type={typeNum}&follow_status={status}&vmid={vmid}&ps={ps}&pn={pn}";
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
