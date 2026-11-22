package com.github.laxika.magicalvibes.service.effect.staticfx;

import org.springframework.stereotype.Component;

/** The source's own half of a supertype grant whose subject names it too. */
@Component
public class GrantSupertypeSelfEffectHandler extends GrantSupertypeEffectHandler {

    public GrantSupertypeSelfEffectHandler(StaticEffectSupport support) {
        super(support);
    }

    @Override
    public boolean selfOnly() {
        return true;
    }
}
