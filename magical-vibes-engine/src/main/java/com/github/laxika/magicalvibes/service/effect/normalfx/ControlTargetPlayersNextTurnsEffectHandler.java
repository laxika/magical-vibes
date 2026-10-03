package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControlTargetPlayersNextTurnsEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ControlTargetPlayersNextTurnsEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ControlTargetPlayersNextTurnsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> targets = entry.getDeclaredTargetIds();
        if (targets.size() < 2 || !entry.isTargetLegal(0) || !entry.isTargetLegal(1)) {
            return;
        }

        UUID firstPlayerId = targets.get(0);
        UUID secondPlayerId = targets.get(1);
        if (firstPlayerId.equals(secondPlayerId)
                || !gameData.playerIds.contains(firstPlayerId)
                || !gameData.playerIds.contains(secondPlayerId)) {
            return;
        }

        gameData.pendingCombatControl.remove(firstPlayerId);
        gameData.pendingCombatControl.remove(secondPlayerId);
        gameData.pendingTurnControl.put(secondPlayerId, firstPlayerId);
        gameData.pendingTurnControl.put(firstPlayerId, secondPlayerId);
        gameData.pendingTurnControlExtraTurn.remove(firstPlayerId);
        gameData.pendingTurnControlExtraTurn.remove(secondPlayerId);

        String firstName = gameData.playerIdToName.get(firstPlayerId);
        String secondName = gameData.playerIdToName.get(secondPlayerId);
        gameLogService.append(gameData, GameLog.text(firstName + " and " + secondName
                + " will control each other's next turns."));
        log.info("Game {} - {} and {} will control each other's next turns",
                gameData.id, firstName, secondName);
    }
}
