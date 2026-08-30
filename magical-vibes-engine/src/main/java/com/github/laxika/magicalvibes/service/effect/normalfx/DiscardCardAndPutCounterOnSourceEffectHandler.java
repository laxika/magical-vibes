package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardAndPutCounterOnSourceEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DiscardCardAndPutCounterOnSourceEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DiscardCardAndPutCounterOnSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DiscardCardAndPutCounterOnSourceEffect) effect;
        UUID controllerId = entry.getControllerId();
        String playerName = gameData.playerIdToName.get(controllerId);
        List<Card> hand = gameData.playerHands.get(controllerId);

        List<Integer> validIndices = new ArrayList<>();
        if (hand != null) {
            UUID sourceCardId = entry.getCard() != null ? entry.getCard().getId() : null;
            for (int i = 0; i < hand.size(); i++) {
                if (predicateEvaluationService.matchesCardPredicate(hand.get(i), e.cardFilter(), sourceCardId)) {
                    validIndices.add(i);
                }
            }
        }

        if (validIndices.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(playerName + " has no "
                    + e.cardDescription() + " to discard."));
            log.info("Game {} - {} has no {} to discard for {}",
                    gameData.id, playerName, e.cardDescription(), entry.getCard().getName());
            return;
        }

        gameData.discardCausedByOpponent = false;
        playerInputService.beginDiscardChoice(gameData, controllerId, validIndices,
                entry.getCard().getName() + " — Choose " + e.cardDescription() + " to discard.",
                1, DiscardFollowUp.plusOnePlusOneCounters(entry.getSourcePermanentId(), 1));

        gameLogService.append(gameData, GameLog.text(playerName + " is choosing "
                + e.cardDescription() + " to discard."));
        log.info("Game {} - {} choosing {} to discard for {}",
                gameData.id, playerName, e.cardDescription(), entry.getCard().getName());
    }
}
