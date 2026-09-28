package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentsAndControllersCloakEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTargetPermanentsAndControllersCloakEffectHandler implements NormalEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetPermanentsAndControllersCloakEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> targetIds = entry.targetsForEffect(effect);
        if (targetIds.isEmpty() && entry.getTargetId() != null) {
            targetIds = List.of(entry.getTargetId());
        }

        for (UUID targetId : targetIds) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null) {
                continue;
            }

            UUID controllerId = gameQueryService.findPermanentController(gameData, targetId);
            if (!permanentRemovalService.removePermanentToExile(gameData, target)) {
                continue;
            }

            gameLogService.append(gameData, GameLog.cardThen(target.getCard(), " is exiled."));
            log.info("Game {} - {} is exiled by {}",
                    gameData.id, target.getCard().getName(), entry.getCard().getName());
            cloakTopCard(gameData, controllerId);
        }

        permanentRemovalService.removeOrphanedAuras(gameData);
    }

    private void cloakTopCard(GameData gameData, UUID controllerId) {
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
