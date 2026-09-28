package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTriggeringPermanentToBattlefieldWithCounterEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Returns a countered creature that triggered Athreos from its graveyard or exile. */
@Component
@RequiredArgsConstructor
public class ReturnTriggeringPermanentToBattlefieldWithCounterEffectHandler
        implements NormalEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;
    private final CreatureControlService creatureControlService;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final GraveyardReturnSupport graveyardReturnSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnTriggeringPermanentToBattlefieldWithCounterEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ReturnTriggeringPermanentToBattlefieldWithCounterEffect returnEffect =
                (ReturnTriggeringPermanentToBattlefieldWithCounterEffect) effect;
        UUID triggeringCardId = entry.getTriggeringCardId();
        UUID controllerId = entry.getControllerId();
        if (triggeringCardId == null || controllerId == null) {
            return;
        }

        if (returnEffect.fromZone() == Zone.GRAVEYARD) {
            returnFromGraveyard(gameData, entry, triggeringCardId, controllerId);
        } else {
            returnFromExile(gameData, entry, triggeringCardId, controllerId);
        }
    }

    private void returnFromGraveyard(GameData gameData, StackEntry entry, UUID cardId,
                                     UUID controllerId) {
        Card card = gameQueryService.findCardInGraveyardById(gameData, cardId);
        UUID ownerId = gameQueryService.findGraveyardOwnerById(gameData, cardId);
        if (card == null || ownerId == null) {
            return;
        }
        long expectedEntryVersion = entry.getTriggeringCardGraveyardEntryVersion();
        if (expectedEntryVersion != 0
                && gameData.graveyardEntryVersion(cardId) != expectedEntryVersion) {
            return;
        }

        permanentRemovalService.removeCardFromGraveyardById(gameData, cardId);
        graveyardReturnSupport.putCardOntoBattlefield(gameData, controllerId, card);
    }

    private void returnFromExile(GameData gameData, StackEntry entry, UUID cardId,
                                 UUID controllerId) {
        ExiledCardEntry exiled = gameData.findExiledCard(cardId);
        if (exiled == null || !gameData.removeFromExile(cardId)) {
            return;
        }

        Card card = exiled.card();
        Permanent permanent = new Permanent(card);
        permanent.setEnteredFromExile(true);
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, permanent);

        if (!controllerId.equals(exiled.ownerId())) {
            gameData.stolenCreatures.put(permanent.getId(), exiled.ownerId());
            creatureControlService.applyControlEffect(gameData, controllerId, permanent,
                    new GainControlOfTargetEffect(ControlDuration.PERMANENT),
                    EffectDuration.PERMANENT, null, entry.getCard().getName());
        }

        String playerName = gameData.playerIdToName.get(controllerId);
        gameLogService.append(gameData, GameLog.cardThen(card,
                " returns to the battlefield under " + playerName + "'s control."));
        battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, controllerId, card, null, false);
    }
}
