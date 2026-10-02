package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches permanents whose name is shared by a permanent that dealt damage to the source's
 * controller during the immediately preceding turn.
 */
public record PermanentSharesNameWithPermanentThatDealtDamageToSourceControllerLastTurnPredicate()
        implements PermanentPredicate {
}
