package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;

import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "73")
public class Iceberg extends Card {

    public Iceberg() {
        // This enchantment enters with X ice counters on it.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.ICE, new XValue()));

        // {3}: Put an ice counter on this enchantment.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}",
                List.of(new PutCountersOnSelfEffect(CounterType.ICE)),
                "{3}: Put an ice counter on this enchantment."
        ));

        // Remove an ice counter from this enchantment: Add {C}.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new RemoveCounterFromSourceCost(1, CounterType.ICE),
                        new AwardManaEffect(ManaColor.COLORLESS)
                ),
                "Remove an ice counter from this enchantment: Add {C}."
        ));
    }
}
