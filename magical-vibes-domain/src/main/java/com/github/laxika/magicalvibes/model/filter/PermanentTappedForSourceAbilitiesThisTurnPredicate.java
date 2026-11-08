package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a permanent that was tapped this turn to pay for an ability of the source permanent ("Merfolk tapped this
 * turn to pay for its abilities", Vodalian War Machine). It reads the source's
 * {@code Permanent.getTappedPermanentsForAbilityThisTurn()}: the source on the battlefield, else the source as it last
 * existed there, which a "when this dies" ability's stack entry keeps as its source snapshot.
 */
public record PermanentTappedForSourceAbilitiesThisTurnPredicate() implements PermanentPredicate {
}
