package com.github.laxika.magicalvibes.service.effect.staticfx;

import org.springframework.stereotype.Component;

@Component
public class BecomeColorlessEffectSelfHandler extends BecomeColorlessEffectHandler {

    public BecomeColorlessEffectSelfHandler(StaticEffectSupport support) {
        super(support);
    }

    @Override
    public boolean selfOnly() {
        return true;
    }
}
