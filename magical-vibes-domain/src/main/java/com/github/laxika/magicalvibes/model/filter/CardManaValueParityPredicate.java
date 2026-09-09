package com.github.laxika.magicalvibes.model.filter;

import com.github.laxika.magicalvibes.model.ManaValueParity;

/** Matches cards whose mana value has the specified odd/even parity. */
public record CardManaValueParityPredicate(ManaValueParity parity) implements CardPredicate {
}
