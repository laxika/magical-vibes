package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControlTargetPlayerNextTurnEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ControlTargetPlayerNextTurnEffectHandler implements NormalEffectHandlerBean {

    private final TurnSupport turnSupport;
    private final GameBroadcastService gameBroadcastService;

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
        gameData.pendingTurnControl.put(targetPlayerId, controllerId);
        if (e.grantExtraTurnAfter()) {
            gameData.pendingTurnControlExtraTurn.add(targetPlayerId);
        } else {
            // Overwriting Mindslaver-style control must not leave a stale Emrakul extra-turn flag.
            gameData.pendingTurnControlExtraTurn.remove(targetPlayerId);
        }

        String controllerName = gameData.playerIdToName.get(controllerId);
        String targetName = gameData.playerIdToName.get(targetPlayerId);
        String logEntry = controllerName + " will control " + targetName + " during their next turn.";
        if (e.grantExtraTurnAfter()) {
            logEntry += " After that turn, " + targetName + " takes an extra turn.";
        }
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} will control {} during their next turn (grantExtraTurnAfter={})",
                gameData.id, controllerName, targetName, e.grantExtraTurnAfter());
    }
}
