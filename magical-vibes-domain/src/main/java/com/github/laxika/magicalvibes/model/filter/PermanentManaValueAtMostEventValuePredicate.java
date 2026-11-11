package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a permanent whose mana value is at most the value of the event the ability triggered on, the stack entry's
 * event value that the {@code EventValue} amount reads: "destroy target artifact or enchantment an opponent controls
 * with mana value less than or equal to that amount of excess damage" (Unleash the Inferno's reflexive ability). The
 * value is {@link FilterContext#eventValue()}, never the ability's X; where the context has none (a spell, an
 * activated ability, a preview) it matches nothing. A face-down permanent has mana value 0.
 */
public record PermanentManaValueAtMostEventValuePredicate() implements PermanentPredicate {
}
