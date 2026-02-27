package top.roozen.bangumi.config;

import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.plugin.ReactiveSettingFetcher;
import top.roozen.bangumi.finder.BangumiFinder;

/**
 * @author <a href="https://roozen.top">Roozen</a>
 * @version 1.0
 * @since 2023/7/30
 */
@Configuration
@RequiredArgsConstructor
public class BangumiPluginConfig {
    private final ReactiveExtensionClient client;
    private final BangumiFinder bangumiFinder;
    private final ReactiveSettingFetcher settingFetcher;

    /**
     * 配置路由函数，处理 /bangumis 路径的 GET 请求
     * 
     * @return RouterFunction<ServerResponse> 路由配置函数
     */
    @Bean
    RouterFunction<ServerResponse> repoTemplateRouter() {
        return RouterFunctions.route()
            .GET("/bangumis/page/{page:\\d+}", request -> {
                String pagePath = request.pathVariable("page");
                int page = NumberUtils.toInt(pagePath, 1);
                int typeNum = NumberUtils.toInt(request.queryParam("typeNum").orElse(null), 1);
                int status = NumberUtils.toInt(request.queryParam("status").orElse(null), 0);

                return settingFetcher.get("base")
                    .map(setting -> setting.get("pageSize").asText("10"))
                    .defaultIfEmpty("10")
                    .flatMap(pageSize -> {
                        boolean sizeFromConfig = !request.queryParam("size").isPresent();
                        int size = request.queryParam("size")
                            .map(s -> NumberUtils.toInt(s, Integer.parseInt(pageSize)))
                            .orElse(Integer.parseInt(pageSize));

                        return bangumiFinder.list(typeNum, status, page, size, sizeFromConfig)
                            .flatMap(bangumiList -> {
                                Map<String, Object> model = new HashMap<>();
                                model.put("bangumis", bangumiList);
                                return ServerResponse.ok().render("bangumis", model);
                            });
                    });
            })
            .GET("/bangumis", request -> {
                int page = NumberUtils.toInt(request.queryParam("page").orElse(null), 1);
                int typeNum = NumberUtils.toInt(request.queryParam("typeNum").orElse(null), 1);
                int status = NumberUtils.toInt(request.queryParam("status").orElse(null), 0);

                return settingFetcher.get("base")
                    .map(setting -> setting.get("pageSize").asText("10"))
                    .defaultIfEmpty("10")
                    .flatMap(pageSize -> {
                        boolean sizeFromConfig = !request.queryParam("size").isPresent();
                        int size = request.queryParam("size")
                            .map(s -> NumberUtils.toInt(s, Integer.parseInt(pageSize)))
                            .orElse(Integer.parseInt(pageSize));

                        return bangumiFinder.list(typeNum, status, page, size, sizeFromConfig)
                            .flatMap(bangumiList -> {
                                Map<String, Object> model = new HashMap<>();
                                model.put("bangumis", bangumiList);
                                return ServerResponse.ok().render("bangumis", model);
                            });
                    });
            })
            .build();
    }
}