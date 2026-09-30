package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ConjureCardNamedOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.StormEffect;

/** Corvid Squall, the prepare spell of Galathul Galecaller // Corvid Squall (YSOS 3). */
public class CorvidSquall extends Card {

    public CorvidSquall() {
        addEffect(EffectSlot.SPELL, new ConjureCardNamedOntoBattlefieldEffect("9ED", "100"));
        addEffect(EffectSlot.ON_SELF_CAST, new StormEffect());
    }
}
