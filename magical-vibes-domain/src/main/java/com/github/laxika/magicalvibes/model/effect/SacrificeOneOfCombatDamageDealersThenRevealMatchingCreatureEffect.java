package com.github.laxika.magicalvibes.model.effect;

import java.util.List;
import java.util.UUID;

/**
 * Sacrifices one of the creatures that dealt combat damage in the triggering damage event, then
 * reveals until a creature card sharing a creature type with the sacrificed creature is found.
 */
public record SacrificeOneOfCombatDamageDealersThenRevealMatchingCreatureEffect(
        List<UUID> combatDamageDealerIds
) implements CombatDamageDealerAwareEffect {

    public SacrificeOneOfCombatDamageDealersThenRevealMatchingCreatureEffect() {
        this(List.of());
    }

    public SacrificeOneOfCombatDamageDealersThenRevealMatchingCreatureEffect {
        combatDamageDealerIds = List.copyOf(combatDamageDealerIds);
    }

    @Override
    public CardEffect withCombatDamageDealerIds(List<UUID> dealerIds) {
        return new SacrificeOneOfCombatDamageDealersThenRevealMatchingCreatureEffect(dealerIds);
    }
}
