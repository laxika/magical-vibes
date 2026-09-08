package com.github.laxika.magicalvibes.service.effect.staticfx;

import org.springframework.stereotype.Component;

/** Self-target half of the global nonland-permanent supertype grant. */
@Component
public class GrantSupertypeToAllNonlandPermanentsSelfEffectHandler
        extends GrantSupertypeToAllNonlandPermanentsEffectHandler {

    @Override
    public boolean selfOnly() {
        return true;
    }
}
