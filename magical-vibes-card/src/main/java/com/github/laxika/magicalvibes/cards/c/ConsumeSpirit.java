package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.DealXDamageToAnyTargetAndGainXLifeEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "131")
public class ConsumeSpirit extends Card {

    public ConsumeSpirit() {
        setNeedsTarget(true);
        setXColorRestriction(ManaColor.BLACK);
        addEffect(EffectSlot.SPELL, new DealXDamageToAnyTargetAndGainXLifeEffect());
    }
}
