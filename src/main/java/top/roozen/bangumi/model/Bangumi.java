package top.roozen.bangumi.model;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

/**
 * @author <a href="https://roozen.top">Roozen</a>
 * @version 1.0
 * @since 2023/7/30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@GVK(group = "bangumi.roozen.top", version = "v1alpha1",
    kind = "Bangumi", singular = "bangumi", plural = "bangumis")
public class Bangumi extends AbstractExtension {
    @Schema(requiredMode = REQUIRED)
    private BangumiSpec spec;

    @Data
    public static class BangumiSpec {
        // title: bangumi?.title,
        // type: bangumi?.season_type_name,
        // area: bangumi?.areas?.[0]?.name,
        // cover,
        // totalCount: total(bangumi?.total_count, typeNum),
        // id: bangumi?.media_id,
        // follow: count(bangumi?.stat?.follow),
        // view: count(bangumi?.stat?.view),
        // danmaku: count(bangumi?.stat?.danmaku),
        // coin: count(bangumi.stat.coin),
        // score: bangumi?.rating?.score ?? '-',
        // des: bangumi?.evaluate
        // @Schema(requiredMode = REQUIRED)
        private String title;
        // @Schema(requiredMode = REQUIRED)
        private String type;
        // @Schema(requiredMode = REQUIRED)
        private String area;
        // @Schema(requiredMode = REQUIRED)
        private String cover;
        // @Schema(requiredMode = REQUIRED)
        private String totalCount;
        // @Schema(requiredMode = REQUIRED)
        private String id;
        // @Schema(requiredMode = REQUIRED)
        private String follow;
        // @Schema(requiredMode = REQUIRED)
        private String view;
        // @Schema(requiredMode = REQUIRED)
        private String danmaku;
        // @Schema(requiredMode = REQUIRED)
        private String coin;
        // @Schema(requiredMode = REQUIRED)
        private String score;
        // @Schema(requiredMode = REQUIRED)
        private String des;
        private String url;


    }

}
