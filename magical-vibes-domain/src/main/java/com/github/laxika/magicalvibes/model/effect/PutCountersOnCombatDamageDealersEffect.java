package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

import java.util.List;
import java.util.UUID;

/**
 * Puts counters on the creatures captured from a batched ally combat-damage trigger.
 * The dealer ids are filled in by {@link AllyCombatDamageTriggerEffect} when the trigger
 * represents "one or more" creatures dealing combat damage in the same step.
 */
public record PutCountersOnCombatDamageDealersEffect(
        CounterType counterType,
        int amount,
        List<UUID> combatDamageDealerIds
) implements CombatDamageDealerAwareEffect {

    public PutCountersOnCombatDamageDealersEffect(CounterType counterType, int amount) {
        this(counterType, amount, List.of());
    }

    public PutCountersOnCombatDamageDealersEffect {
        combatDamageDealerIds = List.copyOf(combatDamageDealerIds);
    }

    @Override
    public CardEffect withCombatDamageDealerIds(List<UUID> dealerIds) {
        return new PutCountersOnCombatDamageDealersEffect(counterType, amount, dealerIds);
    }
}
