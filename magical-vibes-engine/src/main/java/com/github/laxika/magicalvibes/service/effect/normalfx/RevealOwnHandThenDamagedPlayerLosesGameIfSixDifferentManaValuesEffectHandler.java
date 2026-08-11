package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealOwnHandThenDamagedPlayerLosesGameIfSixDifferentManaValuesEffect;
import com.github.laxika.magicalvibes.service.CardRevealService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.outcome.LossOutcome;
import com.github.laxika.magicalvibes.service.outcome.LossReason;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class RevealOwnHandThenDamagedPlayerLosesGameIfSixDifferentManaValuesEffectHandler
        implements NormalEffectHandlerBean {

    private final CardRevealService cardRevealService;
    private final GameLogService gameLogService;
    private final GameOutcomeService gameOutcomeService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealOwnHandThenDamagedPlayerLosesGameIfSixDifferentManaValuesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        UUID losingPlayerId = entry.getTargetId();
        if (controllerId == null || losingPlayerId == null
                || !gameData.playerIds.contains(controllerId)
                || !gameData.playerIds.contains(losingPlayerId)) {
            return;
        }

        cardRevealService.revealHandToAllPlayers(gameData, controllerId);
        List<Card> hand = gameData.playerHands.getOrDefault(controllerId, List.of());
        Set<Integer> manaValues = hand.stream().map(Card::getManaValue).collect(Collectors.toSet());
        if (manaValues.size() < 6) {
            return;
        }

        LossOutcome outcome = gameOutcomeService.resolveLoss(gameData, losingPlayerId, LossReason.EFFECT);
        if (outcome == LossOutcome.PREVENTED) {
            gameLogService.append(gameData,
                    GameLog.text(gameData.playerIdToName.get(losingPlayerId) + " can't lose the game."));
            return;
        }
        if (outcome == LossOutcome.REPLACED) {
            return;
        }

        UUID winnerId = gameQueryService.getOpponentId(gameData, losingPlayerId);
        String loserName = gameData.playerIdToName.get(losingPlayerId);
        gameLogService.append(gameData,
                GameLog.textCardText(loserName + " loses the game from ", entry.getCard(), "."));
        log.info("Game {} - {} loses the game from {}", gameData.id, loserName, entry.getCard().getName());
        gameOutcomeService.declareWinner(gameData, winnerId);
    }
}
