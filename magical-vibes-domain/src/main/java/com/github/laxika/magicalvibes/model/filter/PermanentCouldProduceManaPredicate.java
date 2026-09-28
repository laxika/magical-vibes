package com.github.laxika.magicalvibes.model.filter;

import com.github.laxika.magicalvibes.model.ManaColor;

/** Matches permanents whose current mana abilities could produce the requested mana type. */
public record PermanentCouldProduceManaPredicate(ManaColor manaColor) implements PermanentPredicate {
}
