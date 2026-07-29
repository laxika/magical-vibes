package com.github.laxika.magicalvibes.model.filter;

/**
 * Permanents whose card was cast as a spell by the source's controller this turn ("target creature
 * you cast this turn" — Cycle of Life). Matched by card identity against
 * {@code GameData.getSpellsCastThisTurn(sourceControllerId)}, so a creature that entered without
 * being cast (token, reanimation, Show and Tell) never qualifies, and a creature that changed
 * controllers after being cast still does.
 */
public record PermanentCastBySourceControllerThisTurnPredicate() implements PermanentPredicate {
}
