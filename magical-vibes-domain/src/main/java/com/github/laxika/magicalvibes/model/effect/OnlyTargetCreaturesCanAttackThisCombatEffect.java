package com.github.laxika.magicalvibes.model.effect;

import java.util.Set;
import java.util.UUID;

/**
 * Only the creatures in {@code permanentIds} can attack while this floating effect on every player lasts (CR 508.1c):
 * "during that combat" of an added combat phase, or "this turn" for the pile a player chose (Fight or Flight). A
 * creature that entered after the choice is outside the set; an empty set bars every creature.
 */
public record OnlyTargetCreaturesCanAttackThisCombatEffect(Set<UUID> permanentIds) implements CardEffect {

    public OnlyTargetCreaturesCanAttackThisCombatEffect {
        permanentIds = Set.copyOf(permanentIds);
    }
}
