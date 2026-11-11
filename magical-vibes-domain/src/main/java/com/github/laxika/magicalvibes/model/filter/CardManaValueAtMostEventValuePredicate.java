package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a card whose mana value is at most the value of the event the ability triggered on: "target enchantment,
 * instant, or sorcery card with equal or lesser mana value than that spell" (Saruman of Many Colors, a reflexive
 * ability of a cast trigger, whose event value is the spell's mana value). The "or less" sibling of
 * {@link CardManaValueEqualsEventValuePredicate}, read the same way: from the trigger record as the target is chosen
 * (CR 603.3d) and from the stack entry as the ability resolves, never from the ability's X. Where no event value is
 * given (a spell, an activated ability, a preview) it matches nothing.
 */
public record CardManaValueAtMostEventValuePredicate() implements CardPredicate {
}
