package com.github.laxika.magicalvibes.service.effect.staticfx;

import org.springframework.stereotype.Component;

/**
 * Self-target half of {@link AllPermanentsGainChosenColorEffectHandler}: the layer pass routes the
 * source permanent through the self-handler registry, so this covers Painter's Servant itself
 * gaining its own chosen color. Reuses the parent's apply logic.
 */
@Component
public class AllPermanentsGainChosenColorSelfEffectHandler
        extends AllPermanentsGainChosenColorEffectHandler {

    @Override
    public boolean selfOnly() {
        return true;
    }
}
