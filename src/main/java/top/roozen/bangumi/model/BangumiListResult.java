package top.roozen.bangumi.model;

import java.util.List;
import lombok.Data;

/**
 * 番剧列表结果类，封装番剧分页查询的结果信息
 */
@Data
public class BangumiListResult {
    /**
     * 番剧列表数据
     */
    private final List<Bangumi> items;
    /**
     * 当前页码
     */
    private final int page;
    /**
     * 每页大小
     */
    private final int size;
    /**
     * 总记录数
     */
    private final long total;
    /**
     * 类型编号
     */
    private final int typeNum;
    /**
     * 状态码
     */
    private final int status;
    /**
     * 大小是否来自配置
     */
    private final boolean sizeFromConfig;

    /**
     * 构造函数
     *
     * @param items 番剧列表数据
     * @param page 当前页码
     * @param size 每页大小
     * @param total 总记录数
     * @param typeNum 类型编号
     * @param status 状态码
     * @param sizeFromConfig 大小是否来自配置
     */
    public BangumiListResult(List<Bangumi> items, int page, int size, long total, int typeNum, int status, boolean sizeFromConfig) {
        this.items = items;
        this.page = page;
        this.size = size;
        this.total = total;
        this.typeNum = typeNum;
        this.status = status;
        this.sizeFromConfig = sizeFromConfig;
    }

    /**
     * 构造函数，使用默认的sizeFromConfig值为true
     *
     * @param items 番剧列表数据
     * @param page 当前页码
     * @param size 每页大小
     * @param total 总记录数
     * @param typeNum 类型编号
     * @param status 状态码
     */
    public BangumiListResult(List<Bangumi> items, int page, int size, long total, int typeNum, int status) {
        this(items, page, size, total, typeNum, status, true);
    }

    /**
     * 构造函数，使用默认的typeNum=1, status=0, sizeFromConfig=true
     *
     * @param items 番剧列表数据
     * @param page 当前页码
     * @param size 每页大小
     * @param total 总记录数
     */
    public BangumiListResult(List<Bangumi> items, int page, int size, long total) {
        this(items, page, size, total, 1, 0, true);
    }

    /**
     * 判断是否存在上一页
     *
     * @return 存在上一页返回true，否则返回false
     */
    public boolean hasPrevious() {
        return page > 1;
    }

    /**
     * 判断是否存在下一页
     *
     * @return 存在下一页返回true，否则返回false
     */
    public boolean hasNext() {
        return (long) page * size < total;
    }

    /**
     * 获取上一页的URL
     *
     * @return 上一页的URL字符串，如果不存在上一页则返回null
     */
    public String getPrevUrl() {
        if (!hasPrevious()) {
            return null;
        }
        StringBuilder url = new StringBuilder("?page=" + (page - 1));
        // 如果大小不是来自配置，则在URL中添加size参数
        if (!sizeFromConfig) {
            url.append("&size=").append(size);
        }
        url.append("&typeNum=").append(typeNum).append("&status=").append(status);
        return url.toString();
    }

    /**
     * 获取下一页的URL
     *
     * @return 下一页的URL字符串，如果不存在下一页则返回null
     */
    public String getNextUrl() {
        if (!hasNext()) {
            return null;
        }
        StringBuilder url = new StringBuilder("?page=" + (page + 1));
        // 如果大小不是来自配置，则在URL中添加size参数
        if (!sizeFromConfig) {
            url.append("&size=").append(size);
        }
        url.append("&typeNum=").append(typeNum).append("&status=").append(status);
        return url.toString();
    }

    /**
     * 计算总页数
     *
     * @return 总页数
     */
    public int getTotalPages() {
        return (int) Math.ceil((double) total / size);
    }
}