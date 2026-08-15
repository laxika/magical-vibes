package com.github.laxika.magicalvibes.model.amount;

import com.github.laxika.magicalvibes.model.ManaColor;

/**
 * The amount of unspent mana of a color in the controller's mana pool.
 */
public record UnspentMana(ManaColor color) implements DynamicAmount {
}
