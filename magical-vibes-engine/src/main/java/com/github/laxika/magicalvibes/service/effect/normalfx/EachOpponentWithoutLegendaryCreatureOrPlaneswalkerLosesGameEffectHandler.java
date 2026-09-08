package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentWithoutLegendaryCreatureOrPlaneswalkerLosesGameEffect;
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
public class EachOpponentWithoutLegendaryCreatureOrPlaneswalkerLosesGameEffectHandler
        implements NormalEffectHandlerBean {

    private final GameOutcomeService gameOutcomeService;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachOpponentWithoutLegendaryCreatureOrPlaneswalkerLosesGameEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        for (UUID opponentId : gameData.orderedPlayerIds) {
            if (opponentId.equals(controllerId) || controlsLegendaryCreatureOrPlaneswalker(gameData, opponentId)) {
                continue;
            }

            LossOutcome outcome = gameOutcomeService.resolveLoss(gameData, opponentId, LossReason.EFFECT);
            if (outcome == LossOutcome.PREVENTED) {
                gameLogService.append(gameData, GameLog.text(
                        gameData.playerIdToName.get(opponentId) + " can't lose the game."));
                continue;
            }
            if (outcome == LossOutcome.REPLACED) {
                return;
            }

            UUID winnerId = gameQueryService.getOpponentId(gameData, opponentId);
            String loserName = gameData.playerIdToName.get(opponentId);
            gameLogService.append(gameData, GameLog.textCardText(
                    loserName + " loses the game from ", entry.getCard(), "."));
            gameOutcomeService.declareWinner(gameData, winnerId);
            return;
        }
    }

    private boolean controlsLegendaryCreatureOrPlaneswalker(GameData gameData, UUID playerId) {
        for (Permanent permanent : gameData.playerBattlefields.getOrDefault(playerId, java.util.List.of())) {
            boolean legendaryCreature = gameQueryService.hasEffectiveSupertype(
                    gameData, permanent, CardSupertype.LEGENDARY)
                    && gameQueryService.isCreature(gameData, permanent);
            if (legendaryCreature || gameQueryService.isPlaneswalker(gameData, permanent)) {
                return true;
            }
        }
        return false;
    }
}
