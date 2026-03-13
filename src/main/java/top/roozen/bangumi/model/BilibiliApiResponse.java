package top.roozen.bangumi.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;

/**
 * B站API响应包装类
 *
 * @author <a href="https://github.com/AR-26710">AR-26710</a>
 * @version 1.0
 * @since 2026/03/10
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BilibiliApiResponse {

    private int code;
    private String message;
    private int ttl;
    private DataNode data;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DataNode {
        private List<BilibiliBangumiItem> list;
        private int total;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BilibiliBangumiItem {
        private String title;
        @JsonProperty("season_type_name")
        private String seasonTypeName;
        private String cover;
        @JsonProperty("media_id")
        private String mediaId;
        @JsonProperty("total_count")
        private int totalCount;
        private String evaluate;
        private List<Area> areas;
        private Stat stat;
        private Rating rating;

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Area {
            private int id;
            private String name;
        }

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Stat {
            private long follow;
            private long view;
            private long danmaku;
            private long coin;
        }

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Rating {
            private String score;
        }
    }
}
