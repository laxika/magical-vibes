package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetAuraEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "67")
public class AuraGraft extends Card {

    public AuraGraft() {
        setNeedsTarget(true);
        addEffect(EffectSlot.SPELL, new GainControlOfTargetAuraEffect());
    }
}
