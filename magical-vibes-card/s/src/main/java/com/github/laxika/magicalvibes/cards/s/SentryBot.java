package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.condition.ControllerEnergyAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EnergyCountersEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingSourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "PIP", collectorNumber = "24")
@CardRegistration(set = "PIP", collectorNumber = "371")
@CardRegistration(set = "PIP", collectorNumber = "552")
@CardRegistration(set = "PIP", collectorNumber = "899")
public class SentryBot extends Card {

    public SentryBot() {
        var attackingCreatures = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentIsAttackingSourceControllerPredicate()));

        addEffect(EffectSlot.STATIC, new ReduceOwnCastCostEffect(
                new PermanentCount(attackingCreatures, CountScope.ANY_PLAYER)));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnergyCountersEffect(new PermanentCount(attackingCreatures, CountScope.ANY_PLAYER)));
        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED, new MayEffect(
                ConditionalEffect.unless(new ControllerEnergyAtLeast(3),
                        SequenceEffect.of(
                                new EnergyCountersEffect(-3),
                                new PutCounterOnEachControlledPermanentEffect(
                                        CounterType.PLUS_ONE_PLUS_ONE, 1, new PermanentIsCreaturePredicate()))),
                "Pay {E}{E}{E} to put a +1/+1 counter on each creature you control?"));
    }
}
