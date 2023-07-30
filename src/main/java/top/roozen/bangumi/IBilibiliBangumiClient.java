package top.roozen.bangumi;

import com.fasterxml.jackson.databind.node.ObjectNode;
import reactor.core.publisher.Flux;

/**
 * @author <a href="https://roozen.top">Roozen</a>
 * @version 1.0
 * @since 2023/7/30
 */
public interface IBilibiliBangumiClient {
    Flux<ObjectNode> listBiliBangumiByPage(BilibiliBangumiRequest request);
}
