package com.github.laxika.magicalvibes.model.effect;

import java.util.List;
import java.util.UUID;

/**
 * Returns one of the permanents that dealt combat damage in the triggering damage event to its
 * owner's hand, then resolves the follow-up only after a permanent was returned.
 */
public record ReturnOneOfCombatDamageDealersToHandThenEffect(
        CardEffect thenEffect,
        String permanentDescription,
        List<UUID> combatDamageDealerIds
) implements CombatDamageDealerAwareEffect {

    public ReturnOneOfCombatDamageDealersToHandThenEffect(CardEffect thenEffect, String permanentDescription) {
        this(thenEffect, permanentDescription, List.of());
    }

    public ReturnOneOfCombatDamageDealersToHandThenEffect {
        combatDamageDealerIds = List.copyOf(combatDamageDealerIds);
    }

    @Override
    public CardEffect withCombatDamageDealerIds(List<UUID> dealerIds) {
        return new ReturnOneOfCombatDamageDealersToHandThenEffect(thenEffect, permanentDescription, dealerIds);
    }
}
