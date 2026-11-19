package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.model.effect.SkipStepOrPhaseKind;
import com.github.laxika.magicalvibes.model.effect.SkipChosenStepOrPhaseEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SkipKind;
import com.github.laxika.magicalvibes.model.effect.SkipNextEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resolves {@link SkipNextEffect}: increments the queue matching the effect's {@link SkipKind} for
 * each player {@link SubjectResolutionService} finds for the effect's subject (APNAP order). The turn engine drains each queue when the
 * corresponding turn, step or phase would begin.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SkipNextEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SkipNextEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (SkipNextEffect) effect;

        for (UUID playerId : subjectResolutionService.players(gameData, entry, effect, e.player())) {
            resolveForPlayer(gameData, entry, e, playerId);
        }
    }

    private void resolveForPlayer(GameData gameData, StackEntry entry, SkipNextEffect e,
                                  UUID affectedPlayerId) {
        if (e.allCombatPhasesOfNextTurn()) {
            gameData.addFloatingEffect(FloatingContinuousEffect.onPlayer(entry.getCard().getName(),
                    entry.getSourcePermanentId(), entry.getControllerId(),
                    new SkipChosenStepOrPhaseEffect(SkipStepOrPhaseKind.COMBAT_PHASE), affectedPlayerId,
                    Duration.DURING_AFFECTED_PLAYERS_NEXT_TURN));
        } else {
            // Each "skip your next ..." is its own replacement effect, used up as it applies (CR 614.10, 614.10a).
            gameData.addFloatingEffect(FloatingContinuousEffect.onPlayer(entry.getCard().getName(),
                    entry.getSourcePermanentId(), entry.getControllerId(), e, affectedPlayerId,
                    e.thisTurnOnly() ? Duration.UNTIL_END_OF_TURN : Duration.PERMANENT));
        }

        String affectedName = gameData.playerIdToName.get(affectedPlayerId);
        String phrase = e.allCombatPhasesOfNextTurn()
                ? "skips all combat phases of their next turn" : phraseFor(e.kind());
        gameLogService.append(gameData, GameLog.text(affectedName + " " + phrase + "."));
        log.info("Game {} - {} {}", gameData.id, affectedName, phrase);
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
