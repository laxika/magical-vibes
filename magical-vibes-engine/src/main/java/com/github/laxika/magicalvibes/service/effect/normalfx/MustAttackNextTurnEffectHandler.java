package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MustAttackNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.TauntTarget;
import com.github.laxika.magicalvibes.service.GameLogService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link MustAttackNextTurnEffect}: records the target player and the object their creatures
 * must attack in {@code GameData.tauntedNextTurn}. The turn engine promotes the entry to
 * {@code tauntedThisTurn} when that player's next turn begins.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MustAttackNextTurnEffectHandler implements NormalEffectHandlerBean {

    private final TurnSupport turnSupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MustAttackNextTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (MustAttackNextTurnEffect) effect;
        UUID targetPlayerId = turnSupport.resolveTargetPlayer(gameData, entry);
        if (targetPlayerId == null) {
            return;
        }

        String targetName = gameData.playerIdToName.get(targetPlayerId);

        if (e.tauntTarget() == TauntTarget.SOURCE_PERMANENT) {
            UUID sourcePermanentId = entry.getSourcePermanentId();
            if (sourcePermanentId == null) {
                return;
            }

            gameData.tauntedNextTurn.put(targetPlayerId, sourcePermanentId);

            String sourceName = entry.getCard() != null ? entry.getCard().getName() : "it";
            gameLogService.append(gameData, GameLog.text("Creatures " + targetName + " controls will attack "
                    + sourceName + " if able during their next turn."));
            log.info("Game {} - {} must attack {} next turn", gameData.id, targetName, sourceName);
            return;
        }

        UUID controllerId = entry.getControllerId();
        gameData.tauntedNextTurn.put(targetPlayerId, controllerId);

        String controllerName = gameData.playerIdToName.get(controllerId);
        String logEntry = "Creatures " + targetName + " controls will attack " + controllerName
                + " if able during their next turn.";
        gameLogService.append(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} taunts {} (must attack next turn)", gameData.id, controllerName, targetName);
    }
}
