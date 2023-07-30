package top.roozen.bangumi;

import org.pf4j.PluginWrapper;
import org.springframework.stereotype.Component;
import run.halo.app.extension.SchemeManager;
import run.halo.app.plugin.BasePlugin;

/**
 * @author <a href="https://roozen.top">Roozen</a>
 * @version 1.0
 * @since 2023/7/29
 */
@Component
public class BangumiPlugin extends BasePlugin {

    private final SchemeManager schemeManager;

    public BangumiPlugin(PluginWrapper wrapper, SchemeManager schemeManager) {
        super(wrapper);
        this.schemeManager = schemeManager;
    }

    @Override
    public void start() {
        this.schemeManager.register(Bangumi.class);
    }

    @Override
    public void stop() {
        this.schemeManager.unregister(this.schemeManager.get(Bangumi.class));
    }

    @Override
    public void delete() {
        super.delete();
    }
}
