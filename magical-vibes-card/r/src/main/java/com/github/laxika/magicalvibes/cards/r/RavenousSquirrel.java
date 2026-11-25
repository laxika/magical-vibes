package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MH2", collectorNumber = "211")
@CardRegistration(set = "BLC", collectorNumber = "258")
public class RavenousSquirrel extends Card {

    public RavenousSquirrel() {
        // Whenever you sacrifice an artifact or creature, put a +1/+1 counter on this creature.
        addTrigger(Triggers.sacrificed(
                        new PermanentAnyOfPredicate(List.of(
                                new PermanentIsArtifactPredicate(),
                                new PermanentIsCreaturePredicate())),
                        ObjectRelation.YOU_CONTROL),
                PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE));

        // {1}{B}{G}, Sacrifice an artifact or creature: You gain 1 life and draw a card.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B}{G}",
                List.of(
                        new SacrificePermanentCost(
                                new PermanentAnyOfPredicate(List.of(
                                        new PermanentIsArtifactPredicate(),
                                        new PermanentIsCreaturePredicate()
                                )),
                                "an artifact or creature",
                                false
                        ),
                        new GainLifeEffect(1),
                        new DrawCardEffect(1)
                ),
                "{1}{B}{G}, Sacrifice an artifact or creature: You gain 1 life and draw a card."
        ));
    }
}
