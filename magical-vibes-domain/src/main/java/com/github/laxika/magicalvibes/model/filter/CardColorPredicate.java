package com.github.laxika.magicalvibes.model.filter;

import com.github.laxika.magicalvibes.model.CardColor;

/**
 * Matches a card that <em>is</em> the given colour, i.e. that has {@code color} among its colours
 * ({@code Card.getColors()}). This is the Magic sense of "a red spell" / "a green creature": a
 * multicoloured card is every one of its colours at once, so a Black-Red spell matches both
 * {@code CardColorPredicate(BLACK)} and {@code CardColorPredicate(RED)} (see CR 105.2c).
 *
 * <p>Do not confuse the card's colour <em>list</em> with {@code Card.getColor()}, which returns only
 * the single WUBRG-first colour and would make a Black-Green card match "black" but not "green";
 * this predicate deliberately uses the full colour list so colourhood is order-independent. Used by
 * "whenever a {colour} spell is cast" triggers (Dragon's Claw, Angel's Feather, the mana-rock cycle,
 * the Shrines) and by colour-filtered searches (Green Sun's Zenith).
 */
public record CardColorPredicate(CardColor color) implements CardPredicate {
}
