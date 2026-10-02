package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealMatchingCardsFromTargetHandAndKeepEffect;
import com.github.laxika.magicalvibes.service.CardRevealService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves a filtered public hand reveal followed by a single keep-in-hand choice. */
@Component
@RequiredArgsConstructor
public class RevealMatchingCardsFromTargetHandAndKeepEffectHandler
        implements NormalEffectHandlerBean {

    private final CardRevealService cardRevealService;
    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealMatchingCardsFromTargetHandAndKeepEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var revealEffect = (RevealMatchingCardsFromTargetHandAndKeepEffect) effect;
        UUID targetPlayerId = entry.getTargetId();
        List<Card> hand = gameData.playerHands.getOrDefault(targetPlayerId, List.of());
        UUID sourceCardId = entry.getCard() == null ? null : entry.getCard().getId();
        List<Integer> validIndices = new ArrayList<>();
        List<Card> matchingCards = new ArrayList<>();
        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            if (predicateEvaluationService.matchesCardPredicate(
                    card, revealEffect.filter(), sourceCardId, gameData, targetPlayerId)) {
                validIndices.add(i);
                matchingCards.add(card);
            }
        }

        String targetName = gameData.playerIdToName.get(targetPlayerId);
        GameLog.Builder log = GameLog.builder().text(targetName + " reveals matching cards from their hand");
        if (matchingCards.isEmpty()) {
            log.text(". No cards qualify.");
        } else {
            log.text(": ");
            appendCards(log, matchingCards).text(".");
        }
        gameLogService.append(gameData, log.build());
        cardRevealService.revealToAllPlayers(gameData, targetPlayerId,
                com.github.laxika.magicalvibes.model.event.GameEventFact.RevealZone.HAND, matchingCards);

        if (validIndices.isEmpty()) {
            return;
        }

        interactionHandlerRegistry.begin(gameData, new PendingInteraction.RevealedMatchingHandCardChoice(
                entry.getControllerId(), targetPlayerId, matchingCards,
                revealEffect.chosenCardThenEffect(), "Choose one of the revealed cards.",
                revealEffect.keepInHand()));
    }

    private static GameLog.Builder appendCards(GameLog.Builder builder, List<Card> cards) {
        for (int i = 0; i < cards.size(); i++) {
            if (i > 0) {
                builder.text(", ");
            }
            builder.card(cards.get(i));
        }
        return builder;
    }
}
