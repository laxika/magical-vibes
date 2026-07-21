package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryManaValueEqualsSourceCountersPredicate;

import java.util.List;

@CardRegistration(set = "HOU", collectorNumber = "98")
public class ImminentDoom extends Card {

    public ImminentDoom() {
        // This enchantment enters with a doom counter on it.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EnterWithCountersEffect(
                CounterType.DOOM, new Fixed(1)));

        // Whenever you cast a spell with mana value equal to the number of doom counters on this
        // enchantment, this enchantment deals that much damage to any target. Then put a doom
        // counter on this enchantment.
        // Damage is snapshotted at trigger time (CountersOnSource → Fixed in the collector).
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                null,
                List.of(
                        new DealDamageToAnyTargetEffect(new CountersOnSource(CounterType.DOOM)),
                        new PutCountersOnSelfEffect(CounterType.DOOM)
                ),
                new StackEntryManaValueEqualsSourceCountersPredicate(CounterType.DOOM)
        ));
    }
}
