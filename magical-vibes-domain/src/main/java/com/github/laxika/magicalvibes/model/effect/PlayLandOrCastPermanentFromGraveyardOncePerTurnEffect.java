package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * During each of its controller's turns, permits one matching permanent spell to be cast or one
 * land to be played from that player's graveyard.
 */
public record PlayLandOrCastPermanentFromGraveyardOncePerTurnEffect(
        CardPredicate filter,
        CardPredicate landFilter,
        GrantTriggeredAbilityToCastSpellEffect entryTriggeredAbilityGrant,
        boolean exileIfLeavesBattlefield)
        implements CastSpellsFromGraveyardPermission, PlayLandsFromGraveyardPermission {

    public PlayLandOrCastPermanentFromGraveyardOncePerTurnEffect(
            CardPredicate filter, GrantTriggeredAbilityToCastSpellEffect entryTriggeredAbilityGrant) {
        this(filter, null, entryTriggeredAbilityGrant, false);
    }

    public PlayLandOrCastPermanentFromGraveyardOncePerTurnEffect(
            CardPredicate filter, GrantTriggeredAbilityToCastSpellEffect entryTriggeredAbilityGrant,
            boolean exileIfLeavesBattlefield) {
        this(filter, null, entryTriggeredAbilityGrant, exileIfLeavesBattlefield);
    }

    public PlayLandOrCastPermanentFromGraveyardOncePerTurnEffect(
            CardPredicate filter, CardPredicate landFilter,
            GrantTriggeredAbilityToCastSpellEffect entryTriggeredAbilityGrant,
            boolean exileIfLeavesBattlefield) {
        this.filter = filter;
        this.landFilter = landFilter;
        this.entryTriggeredAbilityGrant = entryTriggeredAbilityGrant;
        this.exileIfLeavesBattlefield = exileIfLeavesBattlefield;
    }

    @Override
    public boolean oncePerControllerTurn() {
        return true;
    }
}
