package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

@CardRegistration(set = "ISD", collectorNumber = "144")
public class Geistflame extends Card {

    public Geistflame() {
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(1));
        addCastingOption(new FlashbackCast("{3}{R}"));
    }
}
