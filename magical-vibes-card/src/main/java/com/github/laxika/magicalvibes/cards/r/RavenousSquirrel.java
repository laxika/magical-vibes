package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "MH2", collectorNumber = "211")
public class RavenousSquirrel extends Card {

    public RavenousSquirrel() {
        // Whenever you sacrifice an artifact or creature, put a +1/+1 counter on this creature.
        addEffect(EffectSlot.ON_ALLY_PERMANENT_SACRIFICED,
                new TriggeringPermanentConditionalEffect(
                        new PermanentAnyOfPredicate(List.of(
                                new PermanentIsArtifactPredicate(),
                                new PermanentIsCreaturePredicate()
                        )),
                        new PutCountersOnSourceEffect(1, 1, 1)
                ));

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
