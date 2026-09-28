package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a permanent when it shares its name with another creature controlled by the source
 * controller or with a creature card in that controller's graveyard.
 */
public record PermanentSharesNameWithControlledCreatureOrGraveyardCreaturePredicate()
        implements PermanentPredicate {
}
