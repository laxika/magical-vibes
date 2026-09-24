package com.github.laxika.magicalvibes.model.effect;

import java.util.List;
import java.util.UUID;

/**
 * Chooses one of the creatures captured from a batched combat-damage event and conjures a
 * duplicate of it into the controller's hand.
 */
public record ConjureDuplicateOfChosenCombatDamageDealerIntoHandEffect(
        List<UUID> combatDamageDealerIds
) implements CombatDamageDealerAwareEffect {

    public ConjureDuplicateOfChosenCombatDamageDealerIntoHandEffect() {
        this(List.of());
    }

    public ConjureDuplicateOfChosenCombatDamageDealerIntoHandEffect {
        combatDamageDealerIds = List.copyOf(combatDamageDealerIds);
    }

    @Override
    public CardEffect withCombatDamageDealerIds(List<UUID> dealerIds) {
        return new ConjureDuplicateOfChosenCombatDamageDealerIntoHandEffect(dealerIds);
    }
}
