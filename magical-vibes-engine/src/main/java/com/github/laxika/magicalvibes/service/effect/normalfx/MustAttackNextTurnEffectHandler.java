package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByPlayerPredicate;
import com.github.laxika.magicalvibes.model.effect.MatchingCreaturesMustAttackEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
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
 * Resolves {@link MustAttackNextTurnEffect}: makes a floating effect that requires the creatures the target player
 * controls to attack the ability's controller, or its source permanent, if able, during that player's next turn
 * (CR 508.1d). It begins as that turn begins and ends at its cleanup step.
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

            requireAttack(gameData, entry, targetPlayerId, sourcePermanentId);

            String sourceName = entry.getCard() != null ? entry.getCard().getName() : "it";
            gameLogService.append(gameData, GameLog.text("Creatures " + targetName + " controls will attack "
                    + sourceName + " if able during their next turn."));
            log.info("Game {} - {} must attack {} next turn", gameData.id, targetName, sourceName);
            return;
        }

        UUID controllerId = entry.getControllerId();
        requireAttack(gameData, entry, targetPlayerId, controllerId);

        String controllerName = gameData.playerIdToName.get(controllerId);
        String logEntry = "Creatures " + targetName + " controls will attack " + controllerName
                + " if able during their next turn.";
        gameLogService.append(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} taunts {} (must attack next turn)", gameData.id, controllerName, targetName);
    }

    private static void requireAttack(GameData gameData, StackEntry entry, UUID targetPlayerId, UUID attackTargetId) {
        gameData.addFloatingEffect(FloatingContinuousEffect.onPlayer(entry.getCard().getName(),
                entry.getSourcePermanentId(), entry.getControllerId(), new MatchingCreaturesMustAttackEffect(
                        new PermanentControlledByPlayerPredicate(targetPlayerId), attackTargetId),
                targetPlayerId, Duration.DURING_AFFECTED_PLAYERS_NEXT_TURN));
    }
}
