package top.roozen.bangumi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import run.halo.app.extension.Metadata;
import run.halo.app.plugin.ReactiveSettingFetcher;
import run.halo.app.theme.finders.Finder;

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
    public List<Bangumi> getBiliData(int typeNum, int status, int ps, int pn) {
        List<ArrayNode> block = bilibiliBangumiClient.listBiliBangumiByPage(
            BilibiliBangumiRequest.builder()
                .vmid(getBiliId().block())
                .typeNum(typeNum)
                .status(status)
                .ps(ps)
                .pn(pn)
                .build()).map((item) -> {
            return item.withArray("/list");
        }).collectList().block();
        return block.stream().flatMap(item -> {
            ArrayList<ObjectNode> objectNodes = new ArrayList<>();
            item.forEach(jsonNode -> objectNodes.add((ObjectNode) jsonNode));
            return objectNodes.stream();
        }).map((item -> {
            Bangumi bangumi = new Bangumi();
            bangumi.setMetadata(new Metadata());
            bangumi.getMetadata().setGenerateName("ban-");
            bangumi.setSpec(new Bangumi.BangumiSpec());
            Bangumi.BangumiSpec spec = bangumi.getSpec();
            spec.setTitle(item.get("title").textValue());
            spec.setType(item.get("season_type_name").textValue());
            ArrayNode jsonNodes = item.withArray("/areas");
            if (jsonNodes != null) {
                JsonNode jsonNode = jsonNodes.get(0);
                if (jsonNode != null) {
                    String s = jsonNode.get("name").textValue();
                    spec.setArea(s);
                }
            }
            spec.setCover(
                item.get("cover").textValue().replaceFirst("^http[^s]$", "https")
                    + "@220w_280h.webp");
            int total_count = item.get("total_count").intValue();
            spec.setTotalCount(total_count >= 0 ? (total_count == -1 ? "未完结"
                : String.format("全%d%s", total_count, typeNum == 1 ? "话" : "集")) : "-");
            spec.setId(item.get("media_id").asText());
            JsonNode stat = item.get("stat");
            spec.setFollow(count(stat.get("follow").asLong()));
            spec.setView(count(stat.get("view").asLong()));
            spec.setDanmaku(count(stat.get("danmaku").asLong()));
            spec.setCoin(count(stat.get("coin").asLong()));
            JsonNode jsonNode = item.get("rating");
            if (jsonNode != null) {
                String score = jsonNode.get("score").asText();
                spec.setScore(score);
            }
            spec.setDes(item.get("evaluate").textValue());
            spec.setUrl("https://www.bilibili.com/bangumi/media/md" + spec.getId());
            return bangumi;
        })).collect(Collectors.toList());
    }

    public List<Bangumi> getBiliDataAll(int typeNum, int status) {
        Integer dataTotal = getDataTotal(typeNum, status);
        ArrayList<Bangumi> res = new ArrayList<>();
        for (int i = 1; i <= (int) Math.ceil(dataTotal / 30) + 1; i++) {
            res.addAll(getBiliData(typeNum, status, 30, 1 * i));
        }
        return res;
    }

    public Integer getDataTotal(int typeNum, int status) {
        List<ObjectNode> block = bilibiliBangumiClient.listBiliBangumiByPage(
            BilibiliBangumiRequest.builder()
                .vmid(getBiliId().block())
                .typeNum(typeNum)
                .status(status)
                .ps(1)
                .pn(1)
                .build()).collectList().block();
        return block.stream().map(item ->
            item.get("total").intValue()
        ).collect(Collectors.toList()).get(0);
    }

    private String count(long num) {
        if (num < 0) {
            return "-";
        }
        if (num > 10000 && num < 100000000) {
            return String.format(Locale.CHINA, "%.1f 万", num / 10000.0);
        } else if (num > 100000000) {
            return String.format(Locale.CHINA, "%.1f 亿", num / 100000000.0);
        } else {
            return num + "";
        }
    }

    Mono<String> getBiliId() {
        return this.settingFetcher.get("base")
            .map(setting -> setting.get("vmid").asText("88704593"))
            .defaultIfEmpty("88704593");
    }
}
