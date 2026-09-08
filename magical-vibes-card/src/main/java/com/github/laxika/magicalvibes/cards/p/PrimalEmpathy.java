package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsCreatureWithGreatestPower;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnChosenOwnPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "IKO", collectorNumber = "200")
public class PrimalEmpathy extends Card {

    public PrimalEmpathy() {
        ControlsCreatureWithGreatestPower greatestPower = new ControlsCreatureWithGreatestPower();
        addEffect(EffectSlot.UPKEEP_TRIGGERED, SequenceEffect.of(
                ConditionalEffect.unless(greatestPower, new DrawCardEffect()),
                ConditionalEffect.unless(new NotCondition(greatestPower),
                        new PutCounterOnChosenOwnPermanentEffect(
                                CounterType.PLUS_ONE_PLUS_ONE, 1, new PermanentIsCreaturePredicate()))));
    }
}
