package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.effect.RemoveCountersEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPowerAtMostPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ECL", collectorNumber = "35")
@CardRegistration(set = "ECL", collectorNumber = "302")
public class SlumberingWalker extends Card {

    public SlumberingWalker() {
        addStaticEffect(new EnterWithCountersEffect(CounterType.MINUS_ONE_MINUS_ONE, new Fixed(2)));

        ReturnCardFromGraveyardEffect returnEffect = ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .filter(new CardAllOfPredicate(List.of(
                        new CardTypePredicate(CardType.CREATURE),
                        new CardPowerAtMostPredicate(2)
                )))
                .targetGraveyard(true)
                .build();
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                new MayEffect(
                        SequenceEffect.of(
                                RemoveCountersEffect.fromSelf(CounterType.ANY, 1),
                                new ConditionalEffect(HappenedThisWay.removedCounters(),
                                        new QueueReflexiveAbilityEffect(returnEffect))),
                        "Remove a counter from this creature?"));
    }
}
