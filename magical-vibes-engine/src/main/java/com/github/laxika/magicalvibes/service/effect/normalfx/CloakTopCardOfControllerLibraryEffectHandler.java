package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CloakTopCardOfControllerLibraryEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves cloaking the top card of the resolving controller's library. */
@Component
@RequiredArgsConstructor
public class CloakTopCardOfControllerLibraryEffectHandler implements NormalEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CloakTopCardOfControllerLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        if (controllerId == null) {
            return;
        }

        List<Card> library = gameData.playerDecks.get(controllerId);
        if (library == null || library.isEmpty()) {
            return;
        }

        Card topCard = library.removeFirst();
        Permanent cloaked = new Permanent(topCard);
        cloaked.setFaceDownAsCloaked();
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, cloaked);
        battlefieldEntryService.processFaceDownCreatureETBTriggers(gameData, controllerId, topCard);

        gameLogService.append(gameData, GameLog.text(
                gameData.playerIdToName.get(controllerId) + " cloaks the top card of their library."));
    }
}
