package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a card that has the same name as a spell any player cast this turn (Twinning Glass's "if it has the same name
 * as a spell that was cast this turn"; the caster doesn't matter). The card and the spell need one name in common: a
 * split card has each half's name (CR 709.4a), a card with all card names shares any spell's, and a spell cast face
 * down has no name (CR 201.2a). Judged on the face being cast (an Adventure's own name, a back face's). Requires the
 * {@code GameData} overload of {@code PredicateEvaluationService.matchesCardPredicate}; without game state it matches
 * nothing.
 */
public record CardSharesNameWithSpellCastThisTurnPredicate() implements CardPredicate {
}
