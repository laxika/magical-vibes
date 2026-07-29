package com.github.laxika.magicalvibes.service.effect.staticfx;

import org.springframework.stereotype.Component;

/**
 * Self-target half of {@link ControlledNonlandPermanentsAreColorEffectHandler}: the layer pass
 * routes the source permanent through the self-handler registry, so this covers the enchantment
 * itself (a nonland permanent you control) taking the color. Reuses the parent's apply logic.
 */
@Component
public class ControlledNonlandPermanentsAreColorSelfEffectHandler
        extends ControlledNonlandPermanentsAreColorEffectHandler {

    @Override
    public boolean selfOnly() {
        return true;
    }
}
