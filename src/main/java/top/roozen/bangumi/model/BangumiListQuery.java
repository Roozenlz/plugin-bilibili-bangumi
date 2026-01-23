package top.roozen.bangumi.model;

import lombok.Data;
import java.util.List;

/**
 * 番剧列表查询对象
 * 用于封装番剧列表查询的各种参数
 */
@Data
public class BangumiListQuery {
    /**
     * 当前页码
     */
    private Integer page;
    /**
     * 每页大小
     */
    private Integer size;
    /**
     * 类型编号
     */
    private Integer typeNum;
    /**
     * 状态
     */
    private Integer status;

    /**
     * 创建番剧列表查询构建器
     *
     * @return BangumiListQueryBuilder 构建器实例
     */
    public static BangumiListQueryBuilder builder() {
        return new BangumiListQueryBuilder();
    }

    /**
     * 番剧列表查询构建器
     * 提供链式调用的方式构建BangumiListQuery对象
     */
    public static class BangumiListQueryBuilder {
        private Integer page;
        private Integer size;
        private Integer typeNum;
        private Integer status;

        /**
         * 设置页码
         *
         * @param page 页码
         * @return BangumiListQueryBuilder 构建器实例
         */
        public BangumiListQueryBuilder page(Integer page) {
            this.page = page;
            return this;
        }

        /**
         * 设置每页大小
         *
         * @param size 每页大小
         * @return BangumiListQueryBuilder 构建器实例
         */
        public BangumiListQueryBuilder size(Integer size) {
            this.size = size;
            return this;
        }

        /**
         * 设置类型编号
         *
         * @param typeNum 类型编号
         * @return BangumiListQueryBuilder 构建器实例
         */
        public BangumiListQueryBuilder typeNum(Integer typeNum) {
            this.typeNum = typeNum;
            return this;
        }

        /**
         * 设置状态
         *
         * @param status 状态
         * @return BangumiListQueryBuilder 构建器实例
         */
        public BangumiListQueryBuilder status(Integer status) {
            this.status = status;
            return this;
        }

        /**
         * 构建番剧列表查询对象
         *
         * @return BangumiListQuery 查询对象实例
         */
        public BangumiListQuery build() {
            BangumiListQuery query = new BangumiListQuery();
            query.page = page;
            query.size = size;
            query.typeNum = typeNum;
            query.status = status;
            return query;
        }
    }
}