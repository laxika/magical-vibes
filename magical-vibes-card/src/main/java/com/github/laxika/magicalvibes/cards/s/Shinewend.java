package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "MOR", collectorNumber = "23")
public class Shinewend extends Card {

    public Shinewend() {
        // This creature enters with a +1/+1 counter on it.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(1)));

        // {1}{W}, Remove a +1/+1 counter from this creature: Destroy target enchantment.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{W}",
                List.of(
                        new RemoveCounterFromSourceCost(1, CounterType.PLUS_ONE_PLUS_ONE),
                        new DestroyTargetPermanentEffect()
                ),
                "{1}{W}, Remove a +1/+1 counter from this creature: Destroy target enchantment.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsEnchantmentPredicate(),
                        "Target must be an enchantment"
                )
        ));
    }
}
