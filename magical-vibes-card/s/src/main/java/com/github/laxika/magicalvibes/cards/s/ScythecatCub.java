package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.condition.NthAbilityResolutionThisTurn;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DoublePlusOneCountersOnTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SLZ", collectorNumber = "85")
@CardRegistration(set = "SLZ", collectorNumber = "206")
@CardRegistration(set = "SLZ", collectorNumber = "327")
public class ScythecatCub extends Card {

    public ScythecatCub() {
        target(TargetFilters.creatureYouControl())
                .addTrigger(Triggers.enters(Triggers.LAND, ObjectRelation.YOU_CONTROL),
                        new ConditionalEffect(
                                new NotCondition(new NthAbilityResolutionThisTurn(2)),
                                new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1)),
                        new ConditionalEffect(
                                new NthAbilityResolutionThisTurn(2),
                                new DoublePlusOneCountersOnTargetCreatureEffect()));
    }
}
