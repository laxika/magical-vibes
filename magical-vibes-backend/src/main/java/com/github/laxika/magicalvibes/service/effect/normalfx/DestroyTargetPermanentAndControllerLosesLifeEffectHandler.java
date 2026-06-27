package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentAndControllerLosesLifeEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DestroyTargetPermanentAndControllerLosesLifeEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameBroadcastService gameBroadcastService;
    private final GameOutcomeService gameOutcomeService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyTargetPermanentAndControllerLosesLifeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DestroyTargetPermanentAndControllerLosesLifeEffect) effect;
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
                if (target == null) {
                    return;
                }

                // Find the controller of the targeted permanent before destruction
                UUID controllerId = gameQueryService.findPermanentController(gameData, target.getId());
                if (controllerId == null) {
                    return;
                }

                // Attempt to destroy the target
                destructionSupport.tryDestroyAndLog(gameData, target, entry.getCard().getName());

                // Target's controller loses life regardless of whether destruction succeeded
                if (!gameQueryService.canPlayerLifeChange(gameData, controllerId)) {
                    gameBroadcastService.logAndBroadcast(gameData,
                            gameData.playerIdToName.get(controllerId) + "'s life total can't change.");
                } else {
                    int currentLife = gameData.getLife(controllerId);
                    gameData.playerLifeTotals.put(controllerId, currentLife - e.lifeLoss());

                    String playerName = gameData.playerIdToName.get(controllerId);
                    String lifeLog = playerName + " loses " + e.lifeLoss() + " life (" + entry.getCard().getName() + ").";
                    gameBroadcastService.logAndBroadcast(gameData, lifeLog);
                    log.info("Game {} - {} loses {} life from {}", gameData.id, playerName, e.lifeLoss(), entry.getCard().getName());
                }

                gameOutcomeService.checkWinCondition(gameData);
    }
}
