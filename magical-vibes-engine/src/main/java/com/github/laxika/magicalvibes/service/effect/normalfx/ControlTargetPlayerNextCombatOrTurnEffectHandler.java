package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.effect.Duration;
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
            gameData.controlPlayer(entry.getCard().getName(), entry.getSourcePermanentId(), controllerId,
                    targetPlayerId, Duration.THROUGH_AFFECTED_PLAYERS_NEXT_TURN, false);
            gameLogService.append(gameData, GameLog.text(controllerName + " will control " + targetName
                    + " during their next turn."));
            log.info("Game {} - {} will control {} during their next turn", gameData.id,
                    controllerName, targetName);
        } else {
            gameData.controlPlayer(entry.getCard().getName(), entry.getSourcePermanentId(), controllerId,
                    targetPlayerId, Duration.DURING_AFFECTED_CONTROLLERS_NEXT_COMBAT, false);
            gameLogService.append(gameData, GameLog.text(controllerName + " will control " + targetName
                    + " during their next combat phase."));
            log.info("Game {} - {} will control {} during their next combat phase", gameData.id,
                    controllerName, targetName);
        }
    }
}
