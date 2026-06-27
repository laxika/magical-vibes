package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetAndControllerLosesLifePerCreatureDeathsEffect;
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
public class DestroyTargetAndControllerLosesLifePerCreatureDeathsEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameBroadcastService gameBroadcastService;
    private final GameOutcomeService gameOutcomeService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyTargetAndControllerLosesLifePerCreatureDeathsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DestroyTargetAndControllerLosesLifePerCreatureDeathsEffect) effect;
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
                if (target == null) {
                    return;
                }

                // Find the controller before destruction
                UUID targetControllerId = gameQueryService.findPermanentController(gameData, entry.getTargetId());
                if (targetControllerId == null) {
                    return;
                }

                // Destroy the target creature
                destructionSupport.tryDestroyAndLog(gameData, target, entry.getCard().getName());

                // Count ALL creatures that died this turn (across all players, including tokens)
                int totalDeaths = 0;
                for (int count : gameData.creatureDeathCountThisTurn.values()) {
                    totalDeaths += count;
                }

                if (totalDeaths > 0) {
                    if (!gameQueryService.canPlayerLifeChange(gameData, targetControllerId)) {
                        gameBroadcastService.logAndBroadcast(gameData,
                                gameData.playerIdToName.get(targetControllerId) + "'s life total can't change.");
                    } else {
                        int currentLife = gameData.getLife(targetControllerId);
                        gameData.playerLifeTotals.put(targetControllerId, currentLife - totalDeaths);

                        String playerName = gameData.playerIdToName.get(targetControllerId);
                        String lifeLog = playerName + " loses " + totalDeaths + " life (" + entry.getCard().getName() + ").";
                        gameBroadcastService.logAndBroadcast(gameData, lifeLog);
                        log.info("Game {} - {} loses {} life from {}", gameData.id, playerName, totalDeaths, entry.getCard().getName());
                    }
                }

                gameOutcomeService.checkWinCondition(gameData);
    }
}
