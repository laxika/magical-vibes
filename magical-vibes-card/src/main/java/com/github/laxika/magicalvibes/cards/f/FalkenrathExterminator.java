package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "AVR", collectorNumber = "134")
public class FalkenrathExterminator extends Card {

    public FalkenrathExterminator() {
        // Whenever this creature deals combat damage to a player, put a +1/+1 counter on it.
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new PutCountersOnSourceEffect(1, 1, 1));

        // {2}{R}: This creature deals damage to target creature equal to the number of
        // +1/+1 counters on this creature.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{R}",
                List.of(new DealDamageToTargetCreatureEffect(
                        new CountersOnSource(CounterType.PLUS_ONE_PLUS_ONE))),
                "{2}{R}: This creature deals damage to target creature equal to the number of +1/+1 counters on this creature.",
                TargetFilters.creature()
        ));
    }
}
