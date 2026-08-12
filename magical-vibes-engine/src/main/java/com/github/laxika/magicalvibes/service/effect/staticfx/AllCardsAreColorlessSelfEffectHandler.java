package com.github.laxika.magicalvibes.service.effect.staticfx;

import org.springframework.stereotype.Component;

/** Covers the source permanent in the global colorless effect. */
@Component
public class AllCardsAreColorlessSelfEffectHandler extends AllCardsAreColorlessEffectHandler {

    @Override
    public boolean selfOnly() {
        return true;
    }
}
