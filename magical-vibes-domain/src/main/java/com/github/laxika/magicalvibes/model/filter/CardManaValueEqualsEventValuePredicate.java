package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a card whose mana value equals the value of the event the ability triggered on: "target instant or sorcery
 * card with mana value equal to the damage dealt" (Soundwave, Sonic Spy), "target creature card with mana value equal
 * to that spell's mana value" (Krile Baldesion). The value is the stack entry's event value, the one the
 * {@code EventValue} amount reads: the engine reads it from the trigger record as the target is chosen (CR 603.3d) and
 * from the entry as the ability resolves, never from the ability's X. Where no event value is given (a spell, an
 * activated ability, a preview) it matches nothing.
 */
public record CardManaValueEqualsEventValuePredicate() implements CardPredicate {
}
