package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * During each of its controller's turns, permits one matching permanent spell to be cast or one
 * land to be played from that player's graveyard.
 */
public record PlayLandOrCastPermanentFromGraveyardOncePerTurnEffect(
        CardPredicate filter,
        GrantTriggeredAbilityToCastSpellEffect entryTriggeredAbilityGrant)
        implements CastSpellsFromGraveyardPermission, PlayLandsFromGraveyardPermission {

    public PlayLandOrCastPermanentFromGraveyardOncePerTurnEffect(
            CardPredicate filter, GrantTriggeredAbilityToCastSpellEffect entryTriggeredAbilityGrant) {
        this.filter = filter;
        this.entryTriggeredAbilityGrant = entryTriggeredAbilityGrant;
    }

    @Override
    public boolean oncePerControllerTurn() {
        return true;
    }

    @Override
    public boolean permitsLandPlayFromGraveyard() {
        return true;
    }

}
