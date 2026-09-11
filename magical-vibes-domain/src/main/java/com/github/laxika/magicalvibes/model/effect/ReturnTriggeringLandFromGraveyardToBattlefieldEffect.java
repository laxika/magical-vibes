package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * When resolved, returns the specified land card from its owner's graveyard to the battlefield under
 * the ability controller's control (if it is still there). The controller of the triggered ability is
 * the graveyard owner, so the land returns under its owner's control.
 *
 * <p>Used by Sacred Ground: "Whenever a spell or ability an opponent controls causes a land to be put
 * into your graveyard from the battlefield, return that card to the battlefield." It is also used by
 * Hedge Shredder for lands put into the graveyard from its controller's library. The trigger collector
 * stamps the concrete {@code landCardId}; the registered template effect carries {@code null}.
 *
 * @param landCardId the UUID of the land card to return from the graveyard
 * @param enterTapped whether the land enters the battlefield tapped
 */
public record ReturnTriggeringLandFromGraveyardToBattlefieldEffect(UUID landCardId, boolean enterTapped)
        implements CardEffect {

    public ReturnTriggeringLandFromGraveyardToBattlefieldEffect(UUID landCardId) {
        this(landCardId, false);
    }
}
