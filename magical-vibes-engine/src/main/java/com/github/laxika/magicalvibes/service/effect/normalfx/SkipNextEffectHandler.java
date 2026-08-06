package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SkipKind;
import com.github.laxika.magicalvibes.model.effect.SkipNextEffect;
import com.github.laxika.magicalvibes.model.effect.SkipRecipient;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

/**
 * Resolves {@link SkipNextEffect}: increments the queue matching the effect's {@link SkipKind} for
 * the player its {@link SkipRecipient} names. The turn engine drains each queue when the
 * corresponding turn, step or phase would begin.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SkipNextEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SkipNextEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (SkipNextEffect) effect;

        UUID affectedPlayerId = e.recipient() == SkipRecipient.CONTROLLER
                ? entry.getControllerId()
                : entry.getTargetId();
        if (affectedPlayerId == null || !gameData.playerIds.contains(affectedPlayerId)) {
            return;
        }

        queueFor(gameData, e.kind()).merge(affectedPlayerId, 1, Integer::sum);

        String affectedName = gameData.playerIdToName.get(affectedPlayerId);
        String phrase = phraseFor(e.kind());
        gameLogService.append(gameData, GameLog.text(affectedName + " " + phrase + "."));
        log.info("Game {} - {} {}", gameData.id, affectedName, phrase);
    }

    private Map<UUID, Integer> queueFor(GameData gameData, SkipKind kind) {
        return switch (kind) {
            case TURN -> gameData.skipNextTurnCount;
            case UNTAP_STEP -> gameData.skipNextUntapStepCount;
            case DRAW_STEP -> gameData.skipNextDrawStepCount;
            case COMBAT_PHASE -> gameData.skipNextCombatPhaseCount;
        };
    }

    private String phraseFor(SkipKind kind) {
        return switch (kind) {
            case TURN -> "will skip their next turn";
            case UNTAP_STEP -> "skips their next untap step";
            case DRAW_STEP -> "will skip their next draw step";
            case COMBAT_PHASE -> "skips their next combat phase";
        };
    }
}
