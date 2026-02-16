package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;

public class AppealToEirdu extends Card {

    public AppealToEirdu() {
        setNeedsTarget(true);
        setMinTargets(1);
        setMaxTargets(2);
        addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(2, 1));
    }
}
