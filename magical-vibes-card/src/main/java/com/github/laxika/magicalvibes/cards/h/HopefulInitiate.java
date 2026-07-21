package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromControlledCreatureCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "INR", collectorNumber = "27")
public class HopefulInitiate extends Card {

    public HopefulInitiate() {
        // Training is driven automatically by the Scryfall-loaded TRAINING keyword.

        // {2}{W}, Remove two +1/+1 counters from among creatures you control: Destroy target artifact or enchantment.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{W}",
                List.of(
                        new RemoveCounterFromControlledCreatureCost(2, CounterType.PLUS_ONE_PLUS_ONE),
                        new DestroyTargetPermanentEffect()
                ),
                "{2}{W}, Remove two +1/+1 counters from among creatures you control: Destroy target artifact or enchantment.",
                new PermanentPredicateTargetFilter(
                        new PermanentAnyOfPredicate(List.of(
                                new PermanentIsArtifactPredicate(),
                                new PermanentIsEnchantmentPredicate()
                        )),
                        "Target must be an artifact or enchantment"
                )
        ));
    }
}
