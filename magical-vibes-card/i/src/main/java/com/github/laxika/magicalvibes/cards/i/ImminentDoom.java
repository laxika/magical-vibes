package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryManaValueEqualsSourceCountersPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "HOU", collectorNumber = "98")
@CardRegistration(set = "AKR", collectorNumber = "161")
public class ImminentDoom extends Card {

    public ImminentDoom() {
        // This enchantment enters with a doom counter on it.
        addStaticEffect(new EnterWithCountersEffect(
                CounterType.DOOM, new Fixed(1)));

        // Whenever you cast a spell with mana value equal to the number of doom counters on this
        // enchantment, this enchantment deals that much damage to any target. Then put a doom
        // counter on this enchantment.
        // Damage is snapshotted at trigger time (CountersOnSource → Fixed in the collector).
        addTrigger(Triggers.youCastSpell()
                        .withStackEntryFilter(new StackEntryManaValueEqualsSourceCountersPredicate(CounterType.DOOM)),
                new DealDamageToAnyTargetEffect(new CountersOnSource(CounterType.DOOM)),
                new PutCountersOnSelfEffect(CounterType.DOOM));
    }
}
