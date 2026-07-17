package com.github.laxika.magicalvibes.model.action;

import java.util.UUID;

/**
 * Delayed trigger (Seraph): at the beginning of the next end step, put the card with {@code cardId}
 * from its owner's graveyard (if still there) onto the battlefield under {@code controllerId}'s
 * control, linked to the source Seraph permanent ({@code seraphPermanentId}) so it is sacrificed if
 * that player later loses control of the Seraph.
 */
public record DelayedGraveyardToBattlefieldUnderControl(UUID cardId, UUID controllerId, UUID seraphPermanentId)
        implements DelayedAction {
}
