package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

@CardRegistration(set = "TSP", collectorNumber = "179")
@CardRegistration(set = "MMA", collectorNumber = "133")
public class SuddenShock extends Card {

    public SuddenShock() {
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(2));
    }
}
