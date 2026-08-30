package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealRandomCardsFromTargetHandDiscardMatchingEffect;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.service.CardRevealService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Component
@RequiredArgsConstructor
public class RevealRandomCardsFromTargetHandDiscardMatchingEffectHandler implements NormalEffectHandlerBean {

    private final CardRevealService cardRevealService;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final GraveyardService graveyardService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealRandomCardsFromTargetHandDiscardMatchingEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RevealRandomCardsFromTargetHandDiscardMatchingEffect) effect;
        List<UUID> targetPlayers = entry.targetsForEffect(e);
        UUID targetPlayerId = targetPlayers.isEmpty() ? entry.getTargetId() : targetPlayers.getFirst();
        if (targetPlayerId == null) {
            return;
        }

        List<Card> hand = gameData.playerHands.get(targetPlayerId);
        String playerName = gameData.playerIdToName.get(targetPlayerId);
        if (hand == null || hand.isEmpty() || e.count() <= 0) {
            gameLogService.append(gameData, GameLog.text(playerName + " has no cards to reveal."));
            return;
        }

        List<Card> cardsAvailable = new ArrayList<>(hand);
        List<Card> revealedCards = new ArrayList<>(Math.min(e.count(), cardsAvailable.size()));
        for (int i = 0; i < e.count() && !cardsAvailable.isEmpty(); i++) {
            revealedCards.add(cardsAvailable.remove(
                    ThreadLocalRandom.current().nextInt(cardsAvailable.size())));
        }

        GameLog.Builder revealBuilder = GameLog.builder().text(playerName + " reveals ");
        appendCardList(revealBuilder, revealedCards);
        revealBuilder.text(" at random.");
        gameLogService.append(gameData, revealBuilder.build());
        cardRevealService.revealToAllPlayers(
                gameData, targetPlayerId, GameEventFact.RevealZone.HAND, revealedCards);

        UUID sourceCardId = entry.getCard() == null ? null : entry.getCard().getId();
        List<Card> discarded = revealedCards.stream()
                .filter(card -> predicateEvaluationService.matchesCardPredicate(
                        card, e.predicate(), sourceCardId, gameData, targetPlayerId))
                .toList();
        if (discarded.isEmpty()) {
            return;
        }

        gameData.discardCausedByOpponent = !targetPlayerId.equals(entry.getControllerId());
        if (gameData.discardCausedByOpponent
                && gameQueryService.isDiscardPrevented(gameData, targetPlayerId)) {
            return;
        }

        hand.removeAll(discarded);
        triggerCollectionService.beginDiscardEvent(gameData, targetPlayerId);
        for (Card card : discarded) {
            graveyardService.discardCard(gameData, targetPlayerId, card);
            triggerCollectionService.checkDiscardTriggers(gameData, targetPlayerId, card);
        }
        triggerCollectionService.finishDiscardEvent(gameData);

        gameLogService.append(gameData, GameLog.text(playerName + " discards " + discarded.size()
                + " revealed matching card" + (discarded.size() == 1 ? "" : "s") + "."));
        log.info("Game {} - {} discards {} revealed matching card(s)",
                gameData.id, playerName, discarded.size());

        if (gameData.hasPendingInteraction(PermanentChoiceContext.DiscardTriggerAnyTarget.class)) {
            triggerCollectionService.processNextDiscardSelfTrigger(gameData);
        }
    }

    private static void appendCardList(GameLog.Builder builder, List<Card> cards) {
        for (int i = 0; i < cards.size(); i++) {
            if (i > 0) {
                builder.text(", ");
            }
            builder.card(cards.get(i));
        }
    }
}
