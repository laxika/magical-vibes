package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnEnchantedPermanentToBattlefieldOnDeathOrExileEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resolves Kaya's Ghostform's return trigger: the stack entry's triggering card, from the zone the enchanted permanent
 * went to as it left the battlefield. The collector records an exile incarnation when it was exiled, else its graveyard
 * incarnation (CR 400.7), so a card that left the graveyard and came back stays where it is.
 */
@Component
@RequiredArgsConstructor
public class ReturnEnchantedPermanentToBattlefieldOnDeathOrExileEffectHandler implements NormalEffectHandlerBean {

    private final PermanentRemovalService permanentRemovalService;
    private final GameQueryService gameQueryService;
    private final GraveyardReturnSupport graveyardReturnSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnEnchantedPermanentToBattlefieldOnDeathOrExileEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID cardId = entry.getTriggeringCardId();
        if (cardId == null) {
            return;
        }

        UUID controllerId = entry.getControllerId();
        if (entry.getTriggeringCardExileEntryVersion() >= 0) {
            returnFromExile(gameData, controllerId, cardId);
            return;
        }
        if (entry.isTriggeringCardIncarnation(gameData.graveyardEntryVersion(cardId))) {
            returnFromGraveyard(gameData, controllerId, cardId);
        }
    }

    private void returnFromGraveyard(GameData gameData, UUID controllerId, UUID cardId) {
        Card card = gameQueryService.findCardInGraveyardById(gameData, cardId);
        UUID ownerId = gameQueryService.findGraveyardOwnerById(gameData, cardId);
        if (card == null || ownerId == null || card.isToken()
                || graveyardReturnSupport.isCardBlockedFromEnteringFromZone(gameData, card, Zone.GRAVEYARD)) {
            return;
        }

        permanentRemovalService.removeCardFromGraveyardById(gameData, cardId);
        Permanent returned = graveyardReturnSupport.putCardOntoBattlefield(
                gameData, controllerId, card, null, null, false, false, null);
        trackNonOwnerReturn(gameData, returned, controllerId, ownerId, true);
    }

    private void returnFromExile(GameData gameData, UUID controllerId, UUID cardId) {
        ExiledCardEntry exiled = gameData.findExiledCard(cardId);
        if (exiled == null || exiled.card().isToken()
                || graveyardReturnSupport.isCardBlockedFromEnteringFromZone(gameData, exiled.card(), Zone.EXILE)
                || !gameData.removeFromExile(cardId)) {
            return;
        }

        Permanent returned = graveyardReturnSupport.putCardOntoBattlefieldFromExile(
                gameData, controllerId, exiled.card());
        trackNonOwnerReturn(gameData, returned, controllerId, exiled.ownerId(), false);
    }

    private void trackNonOwnerReturn(GameData gameData, Permanent returned,
                                     UUID controllerId, UUID ownerId, boolean fromGraveyard) {
        if (returned != null && controllerId != null && !controllerId.equals(ownerId)) {
            if (fromGraveyard) {
                returned.setEnteredFromGraveyardOwnerId(ownerId);
            }
            graveyardReturnSupport.trackStolenCreature(gameData, returned.getId(), controllerId, ownerId);
        }
    }
}
