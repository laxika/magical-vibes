package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * During each of its controller's turns, permits one matching permanent spell to be cast or one
 * land to be played from that player's graveyard. The optional library-origin restriction is used
 * by Kagha, Shadow Archdruid. A permanent cast through the permission may enter tapped.
 */
public record PlayLandOrCastPermanentFromGraveyardOncePerTurnEffect(
        CardPredicate filter,
        CardPredicate landFilter,
        GrantTriggeredAbilityToCastSpellEffect entryTriggeredAbilityGrant,
        boolean onlyCardsPutIntoGraveyardFromLibraryThisTurn,
        boolean entersTapped,
        boolean exileIfLeavesBattlefield)
        implements CastSpellsFromGraveyardPermission, PlayLandsFromGraveyardPermission {

    public PlayLandOrCastPermanentFromGraveyardOncePerTurnEffect(
            CardPredicate filter, GrantTriggeredAbilityToCastSpellEffect entryTriggeredAbilityGrant) {
        this(filter, null, entryTriggeredAbilityGrant, false, false, false);
    }

    public PlayLandOrCastPermanentFromGraveyardOncePerTurnEffect(
            CardPredicate filter, GrantTriggeredAbilityToCastSpellEffect entryTriggeredAbilityGrant,
            boolean exileIfLeavesBattlefield) {
        this(filter, null, entryTriggeredAbilityGrant, false, false, exileIfLeavesBattlefield);
    }

    public PlayLandOrCastPermanentFromGraveyardOncePerTurnEffect(
            CardPredicate filter, CardPredicate landFilter,
            GrantTriggeredAbilityToCastSpellEffect entryTriggeredAbilityGrant,
            boolean exileIfLeavesBattlefield) {
        this(filter, landFilter, entryTriggeredAbilityGrant, false, false, exileIfLeavesBattlefield);
    }

    public PlayLandOrCastPermanentFromGraveyardOncePerTurnEffect(
            CardPredicate filter, GrantTriggeredAbilityToCastSpellEffect entryTriggeredAbilityGrant,
            boolean onlyCardsPutIntoGraveyardFromLibraryThisTurn, boolean entersTapped) {
        this(filter, null, entryTriggeredAbilityGrant,
                onlyCardsPutIntoGraveyardFromLibraryThisTurn, entersTapped, false);
    }

    @Override
    public boolean oncePerControllerTurn() {
        return true;
    }

    @Override
    public boolean permitsLandPlayFromGraveyard() {
        return true;
    }

    @Override
    public CardPredicate landFilter() {
        return landFilter == null ? filter : landFilter;
    }

}
