package top.roozen.bangumi.finder;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.math.NumberUtils;
import reactor.core.publisher.Mono;
import run.halo.app.extension.Metadata;
import run.halo.app.plugin.ReactiveSettingFetcher;
import run.halo.app.theme.finders.Finder;
import top.roozen.bangumi.client.BilibiliBangumiClient;
import top.roozen.bangumi.model.Bangumi;
import top.roozen.bangumi.model.BilibiliApiResponse;
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
     * @param status  番剧状态（0.全部，1.想看，2.在看，3.已看）
     * @param ps      每页数量
     * @param pn      页码
     * @return Bangumi对象列表
     */
    public List<Bangumi> getBiliData(int typeNum, int status, int ps, int pn) {
        return getBiliDataReactive(typeNum, status, ps, pn).block();
    }

    /**
     * 获取所有B站番剧数据（分页获取全部）
     *
     * @param typeNum 番剧类型编号  （1.追番，2.追剧）
     * @param status  番剧状态（0.全部，1.想看，2.在看，3.已看）
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
     * @param status  番剧状态（0.全部，1.想看，2.在看，3.已看）
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
     * @param status  番剧状态（0.全部，1.想看，2.在看，3.已看）
     * @param ps      每页数量
     * @param pn      页码
     * @return Bangumi对象列表的Mono包装
     */
    private Mono<List<Bangumi>> getBiliDataReactive(int typeNum, int status, int ps, int pn) {
        return Mono.zip(
            getBiliId(),
            getUseThumbnailSetting()
        ).flatMap(tuple -> fetchAndParseBangumiList(tuple.getT1(), tuple.getT2(), typeNum, status, ps, pn));
    }

    /**
     * 获取是否使用缩略图的配置
     */
    private Mono<Boolean> getUseThumbnailSetting() {
        return this.settingFetcher.get("base")
            .map(setting -> !setting.get("enable_cover_thumbnail").asBoolean(false))
            .defaultIfEmpty(true);
    }

    /**
     * 获取并解析番剧列表数据
     */
    private Mono<List<Bangumi>> fetchAndParseBangumiList(
        String vmid, boolean useThumbnail, int typeNum, int status, int ps, int pn) {
        BilibiliBangumiRequest request = BilibiliBangumiRequest.builder()
            .vmid(vmid)
            .typeNum(typeNum)
            .status(status)
            .ps(ps)
            .pn(pn)
            .build();

        return bilibiliBangumiClient.listBiliBangumiByPage(request)
            .map(dataNode -> parseBangumiList(dataNode, useThumbnail, typeNum));
    }

    /**
     * 解析番剧列表数据
     */
    private List<Bangumi> parseBangumiList(
        BilibiliApiResponse.DataNode dataNode, boolean useThumbnail, int typeNum) {
        if (dataNode == null || dataNode.getList() == null) {
            return new ArrayList<>();
        }

        List<Bangumi> bangumiList = new ArrayList<>();
        for (BilibiliApiResponse.BilibiliBangumiItem item : dataNode.getList()) {
            if (item != null) {
                bangumiList.add(parseBangumiItem(item, useThumbnail, typeNum));
            }
        }
        return bangumiList;
    }

    /**
     * 解析单个番剧项
     */
    private Bangumi parseBangumiItem(
        BilibiliApiResponse.BilibiliBangumiItem item, boolean useThumbnail, int typeNum) {
        Bangumi bangumi = createBangumi();
        Bangumi.BangumiSpec spec = bangumi.getSpec();

        spec.setId(item.getMediaId());
        spec.setTitle(item.getTitle());
        spec.setType(item.getSeasonTypeName());
        spec.setArea(extractFirstAreaName(item));
        spec.setCover(buildCoverUrl(item.getCover(), useThumbnail));
        spec.setTotalCount(buildTotalCountText(item.getTotalCount(), typeNum));
        spec.setDes(item.getEvaluate());
        spec.setUrl(buildBangumiUrl(item.getMediaId()));

        parseStatData(item, spec);
        parseRatingData(item, spec);

        return bangumi;
    }

    /**
     * 创建空的Bangumi对象
     */
    private Bangumi createBangumi() {
        Bangumi bangumi = new Bangumi();
        bangumi.setMetadata(new Metadata());
        bangumi.getMetadata().setGenerateName("ban-");
        bangumi.setSpec(new Bangumi.BangumiSpec());
        return bangumi;
    }

    /**
     * 提取地区名称
     */
    private String extractFirstAreaName(BilibiliApiResponse.BilibiliBangumiItem item) {
        if (item.getAreas() == null || item.getAreas().isEmpty()) {
            return null;
        }
        BilibiliApiResponse.BilibiliBangumiItem.Area firstArea = item.getAreas().get(0);
        return firstArea != null ? firstArea.getName() : null;
    }

    /**
     * 构建封面URL
     */
    private String buildCoverUrl(String coverUrl, boolean useThumbnail) {
        if (coverUrl == null) {
            coverUrl = "";
        }
        String httpsUrl = coverUrl.startsWith("http://")
            ? coverUrl.replaceFirst("^http://", "https://")
            : coverUrl;
        return useThumbnail ? httpsUrl + "@220w_280h.webp" : httpsUrl;
    }

    /**
     * 构建总集数字符串
     */
    private String buildTotalCountText(int totalCount, int typeNum) {
        if (totalCount < 0) {
            return "-";
        }
        if (totalCount == -1) {
            return "未完结";
        }
        String unit = typeNum == 1 ? "话" : "集";
        return String.format("全%d%s", totalCount, unit);
    }

    /**
     * 解析统计数据
     */
    private void parseStatData(BilibiliApiResponse.BilibiliBangumiItem item, Bangumi.BangumiSpec spec) {
        BilibiliApiResponse.BilibiliBangumiItem.Stat stat = item.getStat();
        if (stat == null) {
            spec.setFollow("-");
            spec.setView("-");
            spec.setDanmaku("-");
            spec.setCoin("-");
            return;
        }

        spec.setFollow(count(stat.getFollow()));
        spec.setView(count(stat.getView()));
        spec.setDanmaku(count(stat.getDanmaku()));
        spec.setCoin(count(stat.getCoin()));
    }

    /**
     * 解析评分数据
     */
    private void parseRatingData(BilibiliApiResponse.BilibiliBangumiItem item, Bangumi.BangumiSpec spec) {
        BilibiliApiResponse.BilibiliBangumiItem.Rating rating = item.getRating();
        if (rating != null) {
            spec.setScore(rating.getScore());
        }
    }

    /**
     * 构建番剧URL
     */
    private String buildBangumiUrl(String mediaId) {
        return "https://www.bilibili.com/bangumi/media/md" + mediaId;
    }

    /**
     * 异步获取数据总数
     *
     * @param typeNum 番剧类型编号（1.追番，2.追剧）
     * @param status  番剧状态（0.全部，1.想看，2.在看，3.已看）
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
                .map(dataNode -> dataNode != null ? dataNode.getTotal() : 0)
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
        int typeNum = toIntSafely(params.get("typeNum"), 1);
        int status = toIntSafely(params.get("status"), 0);
        int page = toIntSafely(params.get("page"), 1);
        int size = toIntSafely(params.get("size"), 10);
        return list(typeNum, status, page, size);
    }

    /**
     * 安全地将对象转换为整数
     *
     * @param value        要转换的值
     * @param defaultValue 默认值
     * @return 转换后的整数值
     */
    private int toIntSafely(Object value, int defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String) {
            return NumberUtils.toInt((String) value, defaultValue);
        }
        return defaultValue;
    }

    /**
     * 分页获取番剧列表（使用默认配置）
     *
     * @param typeNum 番剧类型编号（1.追番，2.追剧）
     * @param status  番剧状态（0.全部，1.想看，2.在看，3.已看）
     * @param page    页码
     * @param size    每页大小
     * @return 番剧列表结果的Mono包装
     */
    public Mono<BangumiListResult> list(int typeNum, int status, int page, int size) {
        return list(typeNum, status, page, size, true);
    }

    /**
     * 分页获取番剧列表
     *
     * @param typeNum        番剧类型编号（1.追番，2.追剧）
     * @param status         番剧状态（0.全部，1.想看，2.在看，3.已看）
     * @param page           页码
     * @param size           每页大小
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
