package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches permanents whose mana value is greater than that of the filter's source card, in whatever zone it is: "a
 * creature you control with greater mana value than this card" on an ability that functions from the graveyard (Kami
 * of Mourning's perpetual grant).
 */
public record PermanentManaValueGreaterThanSourceCardManaValuePredicate() implements PermanentPredicate {
}
