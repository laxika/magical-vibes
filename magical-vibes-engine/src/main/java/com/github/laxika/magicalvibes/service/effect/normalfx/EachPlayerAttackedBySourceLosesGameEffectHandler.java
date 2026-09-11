package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerAttackedBySourceLosesGameEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.outcome.LossOutcome;
import com.github.laxika.magicalvibes.service.outcome.LossReason;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class EachPlayerAttackedBySourceLosesGameEffectHandler implements NormalEffectHandlerBean {

    private final GameOutcomeService gameOutcomeService;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerAttackedBySourceLosesGameEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId == null) {
            return;
        }

        var attackedPlayers = gameData.playersAttackedThisTurn.get(sourcePermanentId);
        if (attackedPlayers == null || attackedPlayers.isEmpty()) {
            return;
        }

        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!attackedPlayers.contains(playerId)) {
                continue;
            }

            LossOutcome outcome = gameOutcomeService.resolveLoss(gameData, playerId, LossReason.EFFECT);
            if (outcome == LossOutcome.PREVENTED) {
                gameLogService.append(gameData, GameLog.text(
                        gameData.playerIdToName.get(playerId) + " can't lose the game."));
                continue;
            }
            if (outcome == LossOutcome.REPLACED) {
                return;
            }

            UUID winnerId = gameQueryService.getOpponentId(gameData, playerId);
            gameLogService.append(gameData, GameLog.textCardText(
                    gameData.playerIdToName.get(playerId) + " loses the game from ", entry.getCard(), "."));
            gameOutcomeService.declareWinner(gameData, winnerId);
            return;
        }
    }
}
