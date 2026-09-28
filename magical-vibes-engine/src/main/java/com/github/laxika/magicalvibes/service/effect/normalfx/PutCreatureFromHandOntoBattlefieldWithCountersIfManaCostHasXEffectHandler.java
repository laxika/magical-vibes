package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCreatureFromHandOntoBattlefieldWithCountersIfManaCostHasXEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
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
public class PutCreatureFromHandOntoBattlefieldWithCountersIfManaCostHasXEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutCreatureFromHandOntoBattlefieldWithCountersIfManaCostHasXEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PutCreatureFromHandOntoBattlefieldWithCountersIfManaCostHasXEffect) effect;
        UUID playerId = entry.getControllerId();
        List<Card> hand = gameData.playerHands.get(playerId);
        List<Integer> validIndices = new ArrayList<>();
        if (hand != null) {
            for (int i = 0; i < hand.size(); i++) {
                if (hand.get(i).hasType(CardType.CREATURE)) {
                    validIndices.add(i);
                }
            }
        }

        if (validIndices.isEmpty()) {
            String playerName = gameData.playerIdToName.get(playerId);
            gameLogService.append(gameData, GameLog.text(playerName + " has no creature cards in hand."));
            log.info("Game {} - {} has no creature cards in hand for hand-to-battlefield effect", gameData.id,
                    playerName);
            return;
        }

        int counterCount = amountEvaluationService.evaluate(gameData, e.counterCount(),
                AmountContext.forStackEntry(entry, null));
        playerInputService.beginCardChoiceWithEntryCounters(gameData, playerId, validIndices,
                "Choose a creature card from your hand to put onto the battlefield.",
                e.counterType(), counterCount, new com.github.laxika.magicalvibes.model.filter.CardHasXInManaCostPredicate());
    }
}
