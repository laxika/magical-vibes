package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashforwardCast;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

@CardRegistration(set = "MB2", collectorNumber = "323")
@CardRegistration(set = "MB2", collectorNumber = "559")
public class Boltfire extends Card {

    public Boltfire() {
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(2));
        addCastingOption(new FlashforwardCast("{4}{R}"));
    }
}
