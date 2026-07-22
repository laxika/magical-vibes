package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardThenEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
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
public class DiscardCardThenEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DiscardCardThenEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DiscardCardThenEffect) effect;

        UUID controllerId = entry.getControllerId();
        String playerName = gameData.playerIdToName.get(controllerId);
        List<Card> hand = gameData.playerHands.get(controllerId);

        List<Integer> validIndices = new ArrayList<>();
        if (hand != null) {
            UUID sourceCardId = entry.getCard() != null ? entry.getCard().getId() : null;
            for (int i = 0; i < hand.size(); i++) {
                if (predicateEvaluationService.matchesCardPredicate(hand.get(i), e.filter(), sourceCardId)) {
                    validIndices.add(i);
                }
            }
        }

        if (validIndices.isEmpty()) {
            String logEntry = playerName + " has no " + e.cardDescription() + " to discard.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} has no {} to discard for {}",
                    gameData.id, playerName, e.cardDescription(), entry.getCard().getName());
            return;
        }

        gameData.discardCausedByOpponent = false;
        playerInputService.beginDiscardChoice(gameData, controllerId, validIndices,
                entry.getCard().getName() + " — Choose " + e.cardDescription() + " to discard.",
                1, DiscardFollowUp.thenEffect(entry.getCard(), e.thenEffect()));

        String logEntry = playerName + " is choosing " + e.cardDescription() + " to discard.";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} choosing {} to discard for {}",
                gameData.id, playerName, e.cardDescription(), entry.getCard().getName());
    }
}
