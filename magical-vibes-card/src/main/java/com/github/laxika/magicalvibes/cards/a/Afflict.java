package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

public class Afflict extends Card {

    public Afflict() {
        setNeedsTarget(true);
        addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(-1, -1));
        addEffect(EffectSlot.SPELL, new DrawCardEffect());
    }
}
