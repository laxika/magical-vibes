package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.DealXDamageToAnyTargetAndGainXLifeEffect;

public class ConsumeSpirit extends Card {

    public ConsumeSpirit() {
        setNeedsTarget(true);
        setXColorRestriction(ManaColor.BLACK);
        addEffect(EffectSlot.SPELL, new DealXDamageToAnyTargetAndGainXLifeEffect());
    }
}
