package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ManaColor;

/**
 * Schedules mandatory mana at the beginning of the controller's next main phase equal to the
 * amount of mana actually spent to cast the targeted spell.
 *
 * @param color the color of mana to add
 */
public record RegisterDelayedManaEqualToTargetSpellManaSpentEffect(ManaColor color) implements CardEffect {
}
