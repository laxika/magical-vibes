package com.github.laxika.magicalvibes.model.filter;

import com.github.laxika.magicalvibes.model.ManaValueParity;

/** Matches permanents whose card mana value has the specified parity. */
public record PermanentManaValueParityPredicate(ManaValueParity parity) implements PermanentPredicate {
}
