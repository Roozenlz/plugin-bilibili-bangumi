package top.roozen.bangumi.finder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.extension.Metadata;
import run.halo.app.plugin.ReactiveSettingFetcher;
import run.halo.app.theme.finders.Finder;
import top.roozen.bangumi.client.BilibiliBangumiClient;
import top.roozen.bangumi.model.Bangumi;
import top.roozen.bangumi.model.BangumiListQuery;
import top.roozen.bangumi.model.BangumiListResult;
import top.roozen.bangumi.request.BilibiliBangumiRequest;

/**
 * @author <a href="https://roozen.top">Roozen</a>
 * @version 1.0
 * @since 2023/7/30
 */
@Finder("bangumiFinder")
@RequiredArgsConstructor
public class BangumiFinder {
    private final BilibiliBangumiClient bilibiliBangumiClient;
    private final ReactiveSettingFetcher settingFetcher;
    
    /**
     * 根据分页参数获取B站番剧数据
     *
     * @param typeNum 番剧类型编号（1.追番，2.追剧）        
     * @param status 番剧状态（0.全部，1.想看，2.在看，3.已看）
     * @param ps 每页数量
     * @param pn 页码
     * @return Bangumi对象列表
     */
    public List<Bangumi> getBiliData(int typeNum, int status, int ps, int pn) {
        return getBiliDataReactive(typeNum, status, ps, pn).block();
    }

    /**
     * 获取所有B站番剧数据（分页获取全部）
     *
     * @param typeNum 番剧类型编号  （1.追番，2.追剧）        
     * @param status 番剧状态（0.全部，1.想看，2.在看，3.已看）
     * @return Bangumi对象列表
     */
    public List<Bangumi> getBiliDataAll(int typeNum, int status) {
        Integer dataTotal = getDataTotal(typeNum, status);
        if (dataTotal == null || dataTotal <= 0) {
            return new ArrayList<>();
        }
        ArrayList<Bangumi> res = new ArrayList<>();
        int totalPages = (int) Math.ceil(dataTotal / 30.0) + 1;
        for (int i = 1; i <= totalPages; i++) {
            res.addAll(getBiliData(typeNum, status, 30, i));
        }
        return res;
    }

    /**
     * 获取指定条件下的数据总数
     *
     * @param typeNum 番剧类型编号（1.追番，2.追剧）        
     * @param status 番剧状态（0.全部，1.想看，2.在看，3.已看）
     * @return 数据总数
     */
    public Integer getDataTotal(int typeNum, int status) {
        return getDataTotalReactive(typeNum, status).block();
    }

    /**
     * 将数字格式化为带单位的字符串（万、亿）
     *
     * @param num 原始数字
     * @return 格式化后的字符串
     */
    private String count(long num) {
        if (num < 0) {
            return "-";
        }
        if (num >= 100000000) {
            return String.format(Locale.CHINA, "%.1f 亿", num / 100000000.0);
        } else if (num >= 10000) {
            return String.format(Locale.CHINA, "%.1f 万", num / 10000.0);
        } else {
            return String.valueOf(num);
        }
    }

    /**
     * 获取B站用户ID配置
     *
     * @return B站用户ID的Mono对象
     */
    Mono<String> getBiliId() {
        return this.settingFetcher.get("base")
            .map(setting -> setting.get("vmid").asText("88704593"))
            .defaultIfEmpty("88704593");
    }

    /**
     * 异步获取B站番剧数据
     *
     * @param typeNum 番剧类型编号（1.追番，2.追剧）
     * @param status 番剧状态（0.全部，1.想看，2.在看，3.已看）
     * @param ps 每页数量
     * @param pn 页码
     * @return Bangumi对象列表的Mono包装
     */
    private Mono<List<Bangumi>> getBiliDataReactive(int typeNum, int status, int ps, int pn) {
        return getBiliId().flatMap(vmid -> 
            bilibiliBangumiClient.listBiliBangumiByPage(
                BilibiliBangumiRequest.builder()
                    .vmid(vmid)
                    .typeNum(typeNum)
                    .status(status)
                    .ps(ps)
                    .pn(pn)
                    .build())
                .map(item -> item.withArray("/list"))
                .flatMapIterable(item -> {
                    ArrayList<ObjectNode> objectNodes = new ArrayList<>();
                    item.forEach(jsonNode -> {
                        if (jsonNode instanceof ObjectNode) {
                            objectNodes.add((ObjectNode) jsonNode);
                        }
                    });
                    return objectNodes;
                })
                .map(item -> {
                    Bangumi bangumi = new Bangumi();
                    bangumi.setMetadata(new Metadata());
                    bangumi.getMetadata().setGenerateName("ban-");
                    bangumi.setSpec(new Bangumi.BangumiSpec());
                    Bangumi.BangumiSpec spec = bangumi.getSpec();
                    
                    JsonNode titleNode = item.get("title");
                    spec.setTitle(titleNode != null ? titleNode.textValue() : "");
                    
                    JsonNode seasonTypeNode = item.get("season_type_name");
                    spec.setType(seasonTypeNode != null ? seasonTypeNode.textValue() : "");
                    
                    ArrayNode areasNode = item.withArray("/areas");
                    if (areasNode != null && areasNode.size() > 0) {
                        JsonNode firstArea = areasNode.get(0);
                        if (firstArea != null) {
                            JsonNode nameNode = firstArea.get("name");
                            if (nameNode != null) {
                                spec.setArea(nameNode.textValue());
                            }
                        }
                    }
                    
                    JsonNode coverNode = item.get("cover");
                    String coverUrl = coverNode != null ? coverNode.textValue() : "";
                    // 修复正则表达式，将HTTP转换为HTTPS
                    String httpsCoverUrl = coverUrl.startsWith("http://") ? 
                        coverUrl.replaceFirst("^http://", "https://") : coverUrl;
                    spec.setCover(httpsCoverUrl + "@220w_280h.webp");
                    
                    JsonNode totalCountNode = item.get("total_count");
                    int total_count = totalCountNode != null ? totalCountNode.intValue() : -1;
                    spec.setTotalCount(total_count >= 0 ? 
                        (total_count == -1 ? "未完结" : 
                         String.format("全%d%s", total_count, typeNum == 1 ? "话" : "集")) : "-");
                    
                    JsonNode mediaIdNode = item.get("media_id");
                    spec.setId(mediaIdNode != null ? mediaIdNode.asText() : "");
                    
                    JsonNode stat = item.get("stat");
                    if (stat != null) {
                        JsonNode followNode = stat.get("follow");
                        spec.setFollow(count(followNode != null ? followNode.asLong() : 0));
                        
                        JsonNode viewNode = stat.get("view");
                        spec.setView(count(viewNode != null ? viewNode.asLong() : 0));
                        
                        JsonNode danmakuNode = stat.get("danmaku");
                        spec.setDanmaku(count(danmakuNode != null ? danmakuNode.asLong() : 0));
                        
                        JsonNode coinNode = stat.get("coin");
                        spec.setCoin(count(coinNode != null ? coinNode.asLong() : 0));
                    } else {
                        spec.setFollow("-");
                        spec.setView("-");
                        spec.setDanmaku("-");
                        spec.setCoin("-");
                    }
                    
                    JsonNode ratingNode = item.get("rating");
                    if (ratingNode != null) {
                        JsonNode scoreNode = ratingNode.get("score");
                        String score = scoreNode != null ? scoreNode.asText() : "";
                        spec.setScore(score);
                    }
                    
                    JsonNode evaluateNode = item.get("evaluate");
                    spec.setDes(evaluateNode != null ? evaluateNode.textValue() : "");
                    
                    spec.setUrl("https://www.bilibili.com/bangumi/media/md" + spec.getId());
                    return bangumi;
                })
                .collectList()
        );
    }

    /**
     * 异步获取数据总数
     *
     * @param typeNum 番剧类型编号（1.追番，2.追剧）
     * @param status 番剧状态（0.全部，1.想看，2.在看，3.已看）
     * @return 数据总数的Mono包装
     */
    private Mono<Integer> getDataTotalReactive(int typeNum, int status) {
        return getBiliId().flatMap(vmid ->
            bilibiliBangumiClient.listBiliBangumiByPage(
                BilibiliBangumiRequest.builder()
                    .vmid(vmid)
                    .typeNum(typeNum)
                    .status(status)
                    .ps(1)
                    .pn(1)
                    .build())
                .next()
                .map(item -> {
                    JsonNode totalNode = item.get("total");
                    return totalNode != null ? totalNode.intValue() : 0;
                })
        );
    }
    /**
     * 统一参数的番剧列表查询方法，支持分页、类型、状态等参数，且均为可选参数。
     *
     * @param params 查询参数，包含：
     *               - page: 分页页码，从 1 开始
     *               - size: 分页条数
     *               - typeNum: 番剧类型编号（1.追番，2.追剧）
     *               - status: 番剧状态（0.全部，1.想看，2.在看，3.已看）
     * @return 番剧列表结果的Mono包装
     */
    public Mono<BangumiListResult> list(Map<String, Object> params) {
        int typeNum = params.containsKey("typeNum") ? ((Number) params.get("typeNum")).intValue() : 1;
        int status = params.containsKey("status") ? ((Number) params.get("status")).intValue() : 0;
        int page = params.containsKey("page") ? ((Number) params.get("page")).intValue() : 1;
        int size = params.containsKey("size") ? ((Number) params.get("size")).intValue() : 10;
        return list(typeNum, status, page, size);
    }

    /**
     * 分页获取番剧列表（使用默认配置）
     *
     * @param typeNum 番剧类型编号（1.追番，2.追剧）
     * @param status 番剧状态（0.全部，1.想看，2.在看，3.已看）
     * @param page 页码
     * @param size 每页大小
     * @return 番剧列表结果的Mono包装
     */
    public Mono<BangumiListResult> list(int typeNum, int status, int page, int size) {
        return list(typeNum, status, page, size, true);
    }

    /**
     * 分页获取番剧列表
     *
     * @param typeNum 番剧类型编号（1.追番，2.追剧）
     * @param status 番剧状态（0.全部，1.想看，2.在看，3.已看）
     * @param page 页码
     * @param size 每页大小
     * @param sizeFromConfig 是否从配置中获取大小
     * @return 番剧列表结果的Mono包装
     */
    public Mono<BangumiListResult> list(int typeNum, int status, int page, int size, boolean sizeFromConfig) {
        int actualPage = page < 1 ? 1 : page;
        int actualSize = size < 1 ? 10 : size;
        return Mono.zip(
            getBiliDataReactive(typeNum, status, actualSize, actualPage),
            getDataTotalReactive(typeNum, status)
        ).map(tuple -> new BangumiListResult(tuple.getT1(), actualPage, actualSize, tuple.getT2(), typeNum, status, sizeFromConfig));
    }
}
