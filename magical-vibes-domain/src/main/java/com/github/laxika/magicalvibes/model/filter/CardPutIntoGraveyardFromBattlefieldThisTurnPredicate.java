package com.github.laxika.magicalvibes.model.filter;

/** Matches a non-token card currently in its owner's graveyard that entered it from the battlefield this turn. */
public record CardPutIntoGraveyardFromBattlefieldThisTurnPredicate() implements CardPredicate {
}
