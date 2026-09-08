package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealHandAndDiscardMatchingCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.service.CardRevealService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class RevealHandAndDiscardMatchingCardsEffectHandler implements NormalEffectHandlerBean {

    private final CardRevealService cardRevealService;
    private final GameLogService gameLogService;
    private final GraveyardService graveyardService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealHandAndDiscardMatchingCardsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RevealHandAndDiscardMatchingCardsEffect) effect;
        UUID targetPlayerId = entry.getTargetId();
        UUID controllerId = entry.getControllerId();
        List<Card> hand = gameData.playerHands.get(targetPlayerId);

        cardRevealService.revealHandToAllPlayers(gameData, targetPlayerId);
        if (hand == null || hand.isEmpty()) {
            return;
        }

        UUID sourceCardId = entry.getCard() == null ? null : entry.getCard().getId();
        List<Card> discarded = new ArrayList<>(hand.stream()
                .filter(card -> predicateEvaluationService.matchesCardPredicate(card, e.predicate(), sourceCardId))
                .toList());
        if (discarded.isEmpty()) {
            String description = CardPredicateUtils.describeFilter(e.predicate());
            gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(targetPlayerId)
                    + " has no " + description + "s to discard."));
            return;
        }

        hand.removeAll(discarded);
        gameData.discardCausedByOpponent = !targetPlayerId.equals(controllerId);
        triggerCollectionService.beginDiscardEvent(gameData, targetPlayerId);
        for (Card card : discarded) {
            graveyardService.discardCard(gameData, targetPlayerId, card);
            triggerCollectionService.checkDiscardTriggers(gameData, targetPlayerId, card);
        }
        triggerCollectionService.finishDiscardEvent(gameData);

        String playerName = gameData.playerIdToName.get(targetPlayerId);
        gameLogService.append(gameData, GameLog.text(playerName + " discards " + discarded.size()
                + " matching card" + (discarded.size() == 1 ? "" : "s") + "."));
        log.info("Game {} - {} discards {} matching card(s)", gameData.id, playerName, discarded.size());
    }
}
