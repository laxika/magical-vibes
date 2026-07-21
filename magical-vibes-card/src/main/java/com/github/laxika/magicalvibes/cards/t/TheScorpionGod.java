package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedReturnCardFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "HOU", collectorNumber = "146")
public class TheScorpionGod extends Card {

    public TheScorpionGod() {
        // Whenever a creature with a -1/-1 counter on it dies, draw a card.
        addEffect(EffectSlot.ON_ANY_CREATURE_DIES, new TriggeringPermanentConditionalEffect(
                new PermanentHasCountersPredicate(CounterType.MINUS_ONE_MINUS_ONE),
                new DrawCardEffect()));

        // {1}{B}{R}: Put a -1/-1 counter on another target creature.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B}{R}",
                List.of(new PutCounterOnTargetPermanentEffect(CounterType.MINUS_ONE_MINUS_ONE, 1)),
                "{1}{B}{R}: Put a -1/-1 counter on another target creature.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentNotPredicate(new PermanentIsSourceCardPredicate()))),
                        "Target must be another creature"
                )
        ));

        // When The Scorpion God dies, return it to its owner's hand at the beginning of the next end step.
        addEffect(EffectSlot.ON_DEATH, new RegisterDelayedReturnCardFromGraveyardToHandEffect(null));
    }
}
