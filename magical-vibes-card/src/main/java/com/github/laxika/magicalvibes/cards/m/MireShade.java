package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "MIR", collectorNumber = "131")
public class MireShade extends Card {

    public MireShade() {
        // {B}, Sacrifice a Swamp: Put a +1/+1 counter on this creature. Activate only as a sorcery.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{B}",
                List.of(
                        new SacrificePermanentCost(
                                new PermanentHasSubtypePredicate(CardSubtype.SWAMP),
                                "Sacrifice a Swamp",
                                false),
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE)),
                "{B}, Sacrifice a Swamp: Put a +1/+1 counter on Mire Shade. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
