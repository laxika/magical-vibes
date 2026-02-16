package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;

public class MightOfOaks extends Card {

    public MightOfOaks() {
        setNeedsTarget(true);
        addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(7, 7));
    }
}
