package com.github.laxika.magicalvibes.model.effect;

import java.util.List;
import java.util.UUID;

/**
 * Effect whose resolution may need the permanent ids of all creatures in a batched combat-damage
 * event that matched its trigger predicate.
 */
public interface CombatDamageDealerAwareEffect extends CardEffect {

    CardEffect withCombatDamageDealerIds(List<UUID> dealerIds);
}
