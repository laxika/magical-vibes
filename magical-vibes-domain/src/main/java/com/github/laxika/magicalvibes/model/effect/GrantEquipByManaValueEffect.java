package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Static effect that grants matching permanents an equip ability with cost equal to their mana value,
 * and makes equipped creatures get +X/+0 where X is the equipment's mana value.
 *
 * <p>When the target matches the filter:
 * <ul>
 *   <li>Grants an equip activated ability with mana cost {X} (X = target's mana value)</li>
 * </ul>
 *
 * <p>When the target is a creature:
 * <ul>
 *   <li>For each attached permanent matching the filter, grants +X/+0 where X = that permanent's mana value</li>
 * </ul>
 *
 * @param filter predicate to determine which permanents receive the equip ability
 */
public record GrantEquipByManaValueEffect(PermanentPredicate filter) implements CardEffect {
}
