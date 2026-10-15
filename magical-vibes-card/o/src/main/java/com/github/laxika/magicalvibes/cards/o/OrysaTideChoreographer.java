package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.FixedIfControlledCreaturesTotalToughnessAtLeast;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;

@CardRegistration(set = "SOS", collectorNumber = "62")
public class OrysaTideChoreographer extends Card {

    public OrysaTideChoreographer() {
        // This spell costs {3} less to cast if creatures you control have total toughness 10 or greater.
        addEffect(EffectSlot.STATIC, new ReduceOwnCastCostEffect(
                new FixedIfControlledCreaturesTotalToughnessAtLeast(10, 3)));

        // When Orysa enters, draw two cards.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DrawCardEffect(2));
    }
}
