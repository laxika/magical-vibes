package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.GreatestPowerAmongControlled;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;

@CardRegistration(set = "DMU", collectorNumber = "139")
public class MoltenMonstrosity extends Card {

    public MoltenMonstrosity() {
        addEffect(EffectSlot.STATIC, new ReduceOwnCastCostEffect(new GreatestPowerAmongControlled()));
    }
}
