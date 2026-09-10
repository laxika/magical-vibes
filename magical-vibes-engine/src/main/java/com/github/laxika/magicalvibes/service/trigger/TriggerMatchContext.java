package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;

import java.util.UUID;

/**
 * Carries the common per-effect match data passed to every trigger collector handler.
 *
 * @param gameData     the current game state
 * @param permanent    the permanent whose effect slot produced the trigger, or {@code null} for an
 *                     effect slot on a card in exile
 * @param controllerId the controller of that permanent/source card
 * @param rawEffect    the original effect from the slot (may be {@code MayEffect}-wrapped)
 * @param sourceCard   the card whose effect slot produced the trigger
 * @param markSourceOncePerTurnOnAcceptance whether accepting this trigger's may ability consumes
 *                                           the source's once-per-turn allowance
 */
public record TriggerMatchContext(
        GameData gameData,
        Permanent permanent,
        UUID controllerId,
        CardEffect rawEffect,
        Card sourceCard,
        PlanarObject sourcePlanarObject,
        boolean markSourceOncePerTurnOnAcceptance
) {

    public TriggerMatchContext(GameData gameData, Permanent permanent, UUID controllerId,
            CardEffect rawEffect) {
        this(gameData, permanent, controllerId, rawEffect, permanent != null ? permanent.getCard() : null, null, false);
    }

    public TriggerMatchContext(GameData gameData, Permanent permanent, UUID controllerId,
            CardEffect rawEffect, Card sourceCard) {
        this(gameData, permanent, controllerId, rawEffect, sourceCard, null, false);
    }

    public TriggerMatchContext(GameData gameData, Permanent permanent, UUID controllerId,
            CardEffect rawEffect, Card sourceCard, boolean markSourceOncePerTurnOnAcceptance) {
        this(gameData, permanent, controllerId, rawEffect, sourceCard, null,
                markSourceOncePerTurnOnAcceptance);
    }

    public TriggerMatchContext(GameData gameData, Permanent permanent, UUID controllerId,
            CardEffect rawEffect, Card sourceCard, PlanarObject sourcePlanarObject) {
        this(gameData, permanent, controllerId, rawEffect, sourceCard, sourcePlanarObject, false);
    }
}
