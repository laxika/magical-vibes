package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

@CardRegistration(set = "THS", collectorNumber = "128")
public class MagmaJet extends Card {

    public MagmaJet() {
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(2));
        addEffect(EffectSlot.SPELL, new ScryEffect(2));
    }
}
