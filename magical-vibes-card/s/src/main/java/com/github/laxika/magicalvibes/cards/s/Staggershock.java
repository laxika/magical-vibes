package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

@CardRegistration(set = "ROE", collectorNumber = "166")
@CardRegistration(set = "IMA", collectorNumber = "147")
@CardRegistration(set = "2X2", collectorNumber = "125")
public class Staggershock extends Card {

    public Staggershock() {
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(2));
    }
}
