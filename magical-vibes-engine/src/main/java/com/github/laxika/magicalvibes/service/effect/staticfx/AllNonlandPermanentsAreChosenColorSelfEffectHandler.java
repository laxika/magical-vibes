package com.github.laxika.magicalvibes.service.effect.staticfx;

import org.springframework.stereotype.Component;

/**
 * Self-target half of {@link AllNonlandPermanentsAreChosenColorEffectHandler}: the layer pass
 * routes the source permanent through the self-handler registry, so this covers the enchantment
 * itself (a nonland permanent) becoming its own chosen color. Reuses the parent's apply logic.
 */
@Component
public class AllNonlandPermanentsAreChosenColorSelfEffectHandler
        extends AllNonlandPermanentsAreChosenColorEffectHandler {

    @Override
    public boolean selfOnly() {
        return true;
    }
}
