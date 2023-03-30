package com.epam.healenium.processor;

import com.epam.healenium.service.CacheService;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;

/**
 * Check for healed locators in cache
 */
@Slf4j
public class CacheElementsProcessor extends FindElementsProcessor {

    public CacheElementsProcessor(BaseProcessor nextProcessor) {
        super(nextProcessor);
    }

    @Override
    public boolean validate() {
        boolean hasElement = !context.getElements().isEmpty();
        if (hasElement) CacheService.remove(engine.getCurrentUrl(), context.getBy());
        return !hasElement;
    }

    @Override
    public void execute() {
        By healed = CacheService.get(engine.getCurrentUrl(), context.getBy());

        if (healed != null) {
            By backup = context.getBy();
            context.setBy(healed);

            // imitate find behavior
            super.execute();

            boolean noElement = context.getElements().isEmpty();
            if (noElement) {
                // clear cache if it cannot be found anymore
                CacheService.remove(engine.getCurrentUrl(), context.getBy());

                // restore locator
                context.setBy(backup);
            }
        }
    }
}
