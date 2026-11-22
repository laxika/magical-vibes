package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerLosesGameEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AsksDeathReplacementsFirst;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.outcome.LossOutcome;
import com.github.laxika.magicalvibes.service.outcome.LossReason;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ControllerLosesGameEffectHandler implements NormalEffectHandlerBean, AsksDeathReplacementsFirst {

    private final GameOutcomeService gameOutcomeService;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ControllerLosesGameEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        for (UUID losingPlayerId : subjectResolutionService.players(gameData, entry, effect,
                ((ControllerLosesGameEffect) effect).player())) {
            if (gameData.playerIds.contains(losingPlayerId) && lose(gameData, entry, losingPlayerId)) {
                return;
            }
        }
    }

    /** Whether the loss ended the game. */
    private boolean lose(GameData gameData, StackEntry entry, UUID losingPlayerId) {
        LossOutcome outcome = gameOutcomeService.resolveLoss(gameData, losingPlayerId, LossReason.EFFECT);
        if (outcome == LossOutcome.PREVENTED) {
            String logEntry = gameData.playerIdToName.get(losingPlayerId) + " can't lose the game.";
            gameLogService.append(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} can't lose the game (protected)", gameData.id,
                    gameData.playerIdToName.get(losingPlayerId));
            return false;
        }
        if (outcome == LossOutcome.REPLACED) {
            // A replacer (Lich's Mirror) already logged and reset the game — nobody wins.
            return false;
        }

        UUID winnerId = gameQueryService.getOpponentId(gameData, losingPlayerId);
        String loserName = gameData.playerIdToName.get(losingPlayerId);

        gameLogService.append(gameData, GameLog.textCardText(loserName + " loses the game from " , entry.getCard(), "."));
        log.info("Game {} - {} loses the game from {}", gameData.id, loserName, entry.getCard().getName());

        gameOutcomeService.declareWinner(gameData, winnerId);
        return true;
    }
}
