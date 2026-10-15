package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "HML", collectorNumber = "24")
public class CoralReef extends Card {

    public CoralReef() {
        // This enchantment enters with four polyp counters on it.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EnterWithCountersEffect(CounterType.POLYP, new Fixed(4)));

        // Sacrifice an Island: Put two polyp counters on this enchantment.
        addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(
                        new SacrificePermanentCost(new PermanentHasSubtypePredicate(CardSubtype.ISLAND), "Sacrifice an Island"),
                        new PutCountersOnSelfEffect(CounterType.POLYP, 2)
                ),
                "Sacrifice an Island: Put two polyp counters on this enchantment."
        ));

        // {U}, Tap an untapped blue creature you control, Remove a polyp counter from this enchantment:
        // Put a +0/+1 counter on target creature.
        addActivatedAbility(new ActivatedAbility(
                false, "{U}",
                List.of(
                        new TapMultiplePermanentsCost(1, new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentColorInPredicate(Set.of(CardColor.BLUE))
                        ))),
                        new RemoveCounterFromSourceCost(1, CounterType.POLYP),
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ZERO_PLUS_ONE, 1)
                ),
                "{U}, Tap an untapped blue creature you control, Remove a polyp counter from this enchantment: Put a +0/+1 counter on target creature.",
                new PermanentPredicateTargetFilter(new PermanentIsCreaturePredicate(), "Target must be a creature")
        ));
    }
}
