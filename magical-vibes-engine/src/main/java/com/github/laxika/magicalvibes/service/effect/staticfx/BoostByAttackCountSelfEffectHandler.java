package com.github.laxika.magicalvibes.service.effect.staticfx;

import org.springframework.stereotype.Component;

@Component
public class BoostByAttackCountSelfEffectHandler extends BoostByAttackCountEffectHandler {

    public BoostByAttackCountSelfEffectHandler(StaticEffectSupport support) {
        super(support);
    }

    @Override
    public boolean selfOnly() {
        return true;
    }
}
