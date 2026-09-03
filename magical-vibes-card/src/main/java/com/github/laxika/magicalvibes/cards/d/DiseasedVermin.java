package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.filter.OpponentPreviouslyDamagedBySourcePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;

@CardRegistration(set = "ALL", collectorNumber = "46")
public class DiseasedVermin extends Card {

    public DiseasedVermin() {
        // Whenever this creature deals combat damage to a player, put an infection counter on it.
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new PutCountersOnSelfEffect(CounterType.INFECTION));

        // At the beginning of your upkeep, this creature deals X damage to target opponent
        // previously dealt damage by it, where X is the number of infection counters on it.
        target(new PlayerPredicateTargetFilter(
                new OpponentPreviouslyDamagedBySourcePredicate(),
                "Target must be an opponent previously dealt damage by this creature"
        ))
                .addEffect(EffectSlot.UPKEEP_TRIGGERED, new DealDamageToPlayersEffect(
                        new CountersOnSource(CounterType.INFECTION), DamageRecipient.TARGET_PLAYER));
    }
}
