package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControlTargetPlayerNextCombatOrTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ControlTargetPlayerNextCombatOrTurnEffectHandler implements NormalEffectHandlerBean {

    private final TurnSupport turnSupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ControlTargetPlayerNextCombatOrTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetPlayerId = turnSupport.resolveTargetPlayer(gameData, entry);
        if (targetPlayerId == null) {
            return;
        }

        UUID controllerId = entry.getControllerId();
        String controllerName = gameData.playerIdToName.get(controllerId);
        String targetName = gameData.playerIdToName.get(targetPlayerId);
        if (entry.isWaterbendCostPaid()) {
            gameData.pendingCombatControl.remove(targetPlayerId);
            gameData.pendingTurnControl.put(targetPlayerId, controllerId);
            gameData.pendingTurnControlExtraTurn.remove(targetPlayerId);
            gameLogService.append(gameData, GameLog.text(controllerName + " will control " + targetName
                    + " during their next turn."));
            log.info("Game {} - {} will control {} during their next turn", gameData.id,
                    controllerName, targetName);
        } else {
            gameData.pendingTurnControl.remove(targetPlayerId);
            gameData.pendingTurnControlExtraTurn.remove(targetPlayerId);
            gameData.pendingCombatControl.put(targetPlayerId, controllerId);
            gameLogService.append(gameData, GameLog.text(controllerName + " will control " + targetName
                    + " during their next combat phase."));
            log.info("Game {} - {} will control {} during their next combat phase", gameData.id,
                    controllerName, targetName);
        }
    }
}
