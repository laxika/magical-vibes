package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "DSK", collectorNumber = "21")
public class OptimisticScavenger extends Card {

    public OptimisticScavenger() {
        PutCounterOnTargetPermanentEffect counterEffect =
                PutCounterOnTargetPermanentEffect.withTargetRestriction(
                        CounterType.PLUS_ONE_PLUS_ONE, 1, new PermanentIsCreaturePredicate());
        addEffect(EffectSlot.ON_ALLY_ENCHANTMENT_ENTERS_BATTLEFIELD, counterEffect);
        addEffect(EffectSlot.ON_ALLY_ROOM_FULLY_UNLOCKED, counterEffect);
    }
}
