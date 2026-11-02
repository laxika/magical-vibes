package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "AVR", collectorNumber = "176")
public class DruidsRepository extends Card {

    public DruidsRepository() {
        // "Whenever a creature you control attacks, put a charge counter on this enchantment."
        // Triggers.creatureYouControlAttacks() fires once per attacking creature; the mandatory effect is sourced
        // by this enchantment, so PutCountersOnSelfEffect lands the counter here, not on the attacker.
        addTrigger(Triggers.creatureYouControlAttacks(), new PutCountersOnSelfEffect(CounterType.CHARGE));

        // "Remove a charge counter from this enchantment: Add one mana of any color."
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new RemoveCounterFromSourceCost(1, CounterType.CHARGE),
                        new AwardAnyColorManaEffect()
                ),
                "Remove a charge counter from this enchantment: Add one mana of any color."
        ));
    }
}
