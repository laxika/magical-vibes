package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CloakTopCardOfDamagedPlayerLibraryEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves a combat-damage trigger that cloaks the damaged player's top library card. */
@Component
@RequiredArgsConstructor
public class CloakTopCardOfDamagedPlayerLibraryEffectHandler implements NormalEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;
    private final GameLogService gameLogService;
    private final GraveyardReturnSupport graveyardReturnSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CloakTopCardOfDamagedPlayerLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID damagedPlayerId = entry.getTargetId();
        UUID controllerId = entry.getControllerId();
        if (damagedPlayerId == null || controllerId == null) {
            return;
        }

        List<Card> library = gameData.playerDecks.get(damagedPlayerId);
        if (library == null || library.isEmpty()) {
            return;
        }

        Card topCard = library.removeFirst();
        Permanent cloaked = new Permanent(topCard);
        cloaked.setFaceDownAsCloaked();
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, cloaked);
        if (!controllerId.equals(damagedPlayerId)) {
            graveyardReturnSupport.trackStolenCreature(
                    gameData, cloaked.getId(), controllerId, damagedPlayerId);
        }
        battlefieldEntryService.processFaceDownCreatureETBTriggers(gameData, controllerId, topCard);

        gameLogService.append(gameData, GameLog.text(
                gameData.playerIdToName.get(controllerId) + " cloaks the top card of "
                        + gameData.playerIdToName.get(damagedPlayerId) + "'s library."));
    }
}
