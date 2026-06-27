package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentAndGiveControllerPoisonCountersEffect;
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
public class DestroyTargetPermanentAndGiveControllerPoisonCountersEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameBroadcastService gameBroadcastService;
    private final GameOutcomeService gameOutcomeService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyTargetPermanentAndGiveControllerPoisonCountersEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DestroyTargetPermanentAndGiveControllerPoisonCountersEffect) effect;
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

                // Give poison counters to the target's controller regardless of whether destruction succeeded
                if (gameQueryService.canPlayerGetPoisonCounters(gameData, controllerId)) {
                    int currentPoison = gameData.playerPoisonCounters.getOrDefault(controllerId, 0);
                    gameData.playerPoisonCounters.put(controllerId, currentPoison + e.poisonCounters());

                    String playerName = gameData.playerIdToName.get(controllerId);
                    String poisonLog = playerName + " gets " + e.poisonCounters() + " poison counter"
                            + (e.poisonCounters() > 1 ? "s" : "") + " (" + entry.getCard().getName() + ").";
                    gameBroadcastService.logAndBroadcast(gameData, poisonLog);
                    log.info("Game {} - {} gets {} poison counter(s) from {}", gameData.id, playerName,
                            e.poisonCounters(), entry.getCard().getName());
                }

                gameOutcomeService.checkWinCondition(gameData);
    }
}
