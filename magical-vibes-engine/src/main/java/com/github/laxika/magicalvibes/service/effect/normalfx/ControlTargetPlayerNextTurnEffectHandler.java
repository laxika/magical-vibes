package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControlTargetPlayerNextTurnEffect;
import com.github.laxika.magicalvibes.service.FaceDownVisibility;
import com.github.laxika.magicalvibes.service.GameLogService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ControlTargetPlayerNextTurnEffectHandler implements NormalEffectHandlerBean {

    private final TurnSupport turnSupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ControlTargetPlayerNextTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ControlTargetPlayerNextTurnEffect) effect;
        UUID targetPlayerId = turnSupport.resolveTargetPlayer(gameData, entry);
        if (targetPlayerId == null) {
            return;
        }

        UUID controllerId = entry.getControllerId();
        gameData.controlPlayer(FaceDownVisibility.sourceName(gameData, entry), entry.getSourcePermanentId(), controllerId, targetPlayerId,
                Duration.THROUGH_AFFECTED_PLAYERS_NEXT_TURN, e.grantExtraTurnAfter());

        String controllerName = gameData.playerIdToName.get(controllerId);
        String targetName = gameData.playerIdToName.get(targetPlayerId);
        String logEntry = controllerName + " will control " + targetName + " during their next turn.";
        if (e.grantExtraTurnAfter()) {
            logEntry += " After that turn, " + targetName + " takes an extra turn.";
        }
        gameLogService.append(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} will control {} during their next turn (grantExtraTurnAfter={})",
                gameData.id, controllerName, targetName, e.grantExtraTurnAfter());
    }
}
