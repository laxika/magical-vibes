package com.github.laxika.magicalvibes.model.effect;

import java.util.List;
import java.util.UUID;

/**
 * Lets the controller choose one of the creatures from the triggering combat-damage event to
 * sacrifice, then reveals until a creature card sharing a creature type with it is found.
 */
public record SacrificeOneOfCombatDamageDealersThenRevealUntilSharedCreatureTypeEffect(
        List<UUID> combatDamageDealerIds
) implements CombatDamageDealerAwareEffect {

    public SacrificeOneOfCombatDamageDealersThenRevealUntilSharedCreatureTypeEffect() {
        this(List.of());
    }

    public SacrificeOneOfCombatDamageDealersThenRevealUntilSharedCreatureTypeEffect {
        combatDamageDealerIds = List.copyOf(combatDamageDealerIds);
    }

    @Override
    public CardEffect withCombatDamageDealerIds(List<UUID> dealerIds) {
        return new SacrificeOneOfCombatDamageDealersThenRevealUntilSharedCreatureTypeEffect(dealerIds);
    }
}
