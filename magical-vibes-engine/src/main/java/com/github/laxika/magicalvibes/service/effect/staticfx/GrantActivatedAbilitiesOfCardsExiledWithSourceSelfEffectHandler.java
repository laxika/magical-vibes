package com.github.laxika.magicalvibes.service.effect.staticfx;

import org.springframework.stereotype.Component;

/** The source's own half of the exiled cards' abilities grant, for a subject that names it too. */
@Component
public class GrantActivatedAbilitiesOfCardsExiledWithSourceSelfEffectHandler
        extends GrantActivatedAbilitiesOfCardsExiledWithSourceEffectHandler {

    public GrantActivatedAbilitiesOfCardsExiledWithSourceSelfEffectHandler(StaticEffectSupport support) {
        super(support);
    }

    @Override
    public boolean selfOnly() {
        return true;
    }
}
