package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnEarthbendedLandEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Returns an Earthbended land from the zone it entered after leaving the battlefield. */
@Component
@RequiredArgsConstructor
public class ReturnEarthbendedLandEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnEarthbendedLandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ReturnEarthbendedLandEffect returnEffect = (ReturnEarthbendedLandEffect) effect;
        UUID cardId = entry.getCard().getId();
        UUID controllerId = returnEffect.returnControllerId();
        if (returnEffect.fromExile()) {
            returnFromExile(gameData, entry, cardId, controllerId);
        } else {
            returnFromGraveyard(gameData, entry, cardId, controllerId);
        }
    }

    private void returnFromExile(GameData gameData, StackEntry entry, UUID cardId,
                                 UUID controllerId) {
        ExiledCardEntry exiled = gameData.findExiledCard(cardId);
        if (exiled == null || !gameData.removeFromExile(cardId)) {
            return;
        }

        Card card = exiled.card();
        Permanent permanent = new Permanent(card);
        permanent.tap();
        permanent.setEnteredFromExile(true);
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, permanent);
        gameLogService.append(gameData, GameLog.textCardText(
                gameData.playerIdToName.get(controllerId) + " returns ", card,
                " to the battlefield tapped."));
        battlefieldEntryService.handleCreatureEnteredBattlefield(
                gameData, controllerId, card, null, false);
    }

    private void returnFromGraveyard(GameData gameData, StackEntry entry, UUID cardId,
                                     UUID controllerId) {
        Card card = gameQueryService.findCardInGraveyardById(gameData, cardId);
        UUID graveyardOwnerId = gameQueryService.findGraveyardOwnerById(gameData, cardId);
        if (card == null || graveyardOwnerId == null
                || graveyardReturnSupport.isCardBlockedFromEnteringFromZone(gameData, card, Zone.GRAVEYARD)) {
            return;
        }
        permanentRemovalService.removeCardFromGraveyardById(gameData, cardId);

        Permanent permanent = new Permanent(card);
        permanent.tap();
        permanent.setEnteredFromGraveyardOwnerId(graveyardOwnerId);
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, permanent);
        gameLogService.append(gameData, GameLog.textCardText(
                gameData.playerIdToName.get(controllerId) + " returns ", card,
                " to the battlefield tapped."));
        graveyardReturnSupport.handleCreatureEtbAndLegendRule(
                gameData, controllerId, permanent, card);
    }
}
