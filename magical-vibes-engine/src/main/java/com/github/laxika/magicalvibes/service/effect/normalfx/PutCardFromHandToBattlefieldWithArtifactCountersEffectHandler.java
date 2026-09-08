package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardFromHandToBattlefieldWithArtifactCountersEffect;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PutCardFromHandToBattlefieldWithArtifactCountersEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutCardFromHandToBattlefieldWithArtifactCountersEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PutCardFromHandToBattlefieldWithArtifactCountersEffect) effect;
        UUID playerId = entry.getControllerId();
        List<Card> hand = gameData.playerHands.get(playerId);
        List<Integer> validIndices = new ArrayList<>();
        UUID sourceCardId = entry.getCard() == null ? null : entry.getCard().getId();
        if (hand != null) {
            for (int i = 0; i < hand.size(); i++) {
                if (predicateEvaluationService.matchesCardPredicate(hand.get(i), e.predicate(), sourceCardId,
                        gameData, playerId)) {
                    validIndices.add(i);
                }
            }
        }

        if (validIndices.isEmpty()) {
            String playerName = gameData.playerIdToName.get(playerId);
            gameLogService.append(gameData, GameLog.text(playerName + " has no " + e.label() + " cards in hand."));
            log.info("Game {} - {} has no {} cards in hand for hand-to-battlefield effect", gameData.id,
                    playerName, e.label());
            return;
        }

        playerInputService.beginCardChoiceWithArtifactCounters(gameData, playerId, validIndices,
                "Choose a " + e.label() + " card from your hand to put onto the battlefield.",
                CounterType.PLUS_ONE_PLUS_ONE, e.counterCount());
    }
}
