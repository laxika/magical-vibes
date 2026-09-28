package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * During each of its controller's turns, permits one matching permanent spell to be cast or one
 * land to be played from that player's graveyard. The optional library-origin restriction is used
 * by Kagha, Shadow Archdruid.
 */
public record PlayLandOrCastPermanentFromGraveyardOncePerTurnEffect(
        CardPredicate filter,
        GrantTriggeredAbilityToCastSpellEffect entryTriggeredAbilityGrant,
        boolean onlyCardsPutIntoGraveyardFromLibraryThisTurn)
        implements CastSpellsFromGraveyardPermission, PlayLandsFromGraveyardPermission {

    public PlayLandOrCastPermanentFromGraveyardOncePerTurnEffect(
            CardPredicate filter, GrantTriggeredAbilityToCastSpellEffect entryTriggeredAbilityGrant) {
        this(filter, entryTriggeredAbilityGrant, false);
    }

    @Override
    public boolean oncePerControllerTurn() {
        return true;
    }
}
