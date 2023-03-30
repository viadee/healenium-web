package com.epam.healenium.processor;

import com.epam.healenium.service.CacheService;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;

/**
 * Check for healed locators in cache
 */
@Slf4j
public class CacheChildElementProcessor extends FindChildElementProcessor {

    public CacheChildElementProcessor(BaseProcessor nextProcessor) {
        super(nextProcessor);
    }

    @Override
    public boolean validate() {
        boolean hasElement = context.getNoSuchElementException() == null;
        if (hasElement) CacheService.remove(engine.getCurrentUrl(), context.getBy());
        return !hasElement;
    }

    @Override
    public void execute() {
        By healed = CacheService.get(engine.getCurrentUrl(), context.getBy());

        if (healed != null) {
            By backup = context.getBy();
            context.setBy(healed);

            try {
                // imitate find behavior
                find();

                // avoid running the self-healing algorithm
                context.setNoSuchElementException(null);
            } catch (NoSuchElementException ignored) {
                // clear cache if it cannot be found anymore
                CacheService.remove(engine.getCurrentUrl(), context.getBy());

                // restore locator
                context.setBy(backup);
            }
        }
    }
}
