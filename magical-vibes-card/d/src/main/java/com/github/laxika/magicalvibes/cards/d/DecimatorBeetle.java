package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveAndPutCounterOnAttackEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "AKH", collectorNumber = "197")
public class DecimatorBeetle extends Card {

    public DecimatorBeetle() {
        // "When this creature enters, put a -1/-1 counter on target creature you control."
        target(TargetFilters.creatureYouControl()).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new PutCounterOnTargetPermanentEffect(CounterType.MINUS_ONE_MINUS_ONE, 1));

        // "Whenever this creature attacks, remove a -1/-1 counter from target creature you control and
        // put a -1/-1 counter on up to one target creature defending player controls." The two targets
        // are chosen sequentially through the bespoke attack counter-move pipeline (CombatAttackService).
        addEffect(EffectSlot.ON_ATTACK, new RemoveAndPutCounterOnAttackEffect());
    }
}
