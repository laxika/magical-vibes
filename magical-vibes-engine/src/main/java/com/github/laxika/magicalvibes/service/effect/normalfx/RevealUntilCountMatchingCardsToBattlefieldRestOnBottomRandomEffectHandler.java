package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealUntilCountMatchingCardsToBattlefieldRestOnBottomRandomEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/** Resolves the reveal-until-a-count-of-matching-cards effect. */
@Slf4j
@Component
@RequiredArgsConstructor
public class RevealUntilCountMatchingCardsToBattlefieldRestOnBottomRandomEffectHandler
        implements NormalEffectHandlerBean {

    private final AmountEvaluationService amountEvaluationService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealUntilCountMatchingCardsToBattlefieldRestOnBottomRandomEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var typedEffect = (RevealUntilCountMatchingCardsToBattlefieldRestOnBottomRandomEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);
        int requiredCount = amountEvaluationService.evaluate(
                gameData,
                typedEffect.requiredCount(),
                AmountContext.forStackEntry(entry, null));

        if (requiredCount <= 0 || deck == null || deck.isEmpty()) {
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                    ": " + playerName + " reveals no cards."));
            return;
        }

        List<Card> revealedCards = new ArrayList<>();
        List<Card> matchingCards = new ArrayList<>();
        while (!deck.isEmpty() && matchingCards.size() < requiredCount) {
            Card card = deck.removeFirst();
            revealedCards.add(card);
            if (predicateEvaluationService.matchesCardPredicate(
                    card, typedEffect.predicate(), entry.getCard().getId(), gameData, controllerId)) {
                matchingCards.add(card);
            }
        }

        gameLogService.append(gameData, GameLog.text(
                playerName + " reveals " + revealedCards.size()
                        + (revealedCards.size() == 1 ? " card" : " cards")
                        + " from the top of their library."));

        if (matchingCards.isEmpty()) {
            putRevealedCardsOnBottom(gameData, controllerId, revealedCards);
            gameLogService.append(gameData, GameLog.text(
                    playerName + " finds no matching cards. The revealed cards are put on the bottom of their library in a random order."));
            return;
        }

        List<UUID> matchingIds = matchingCards.stream().map(Card::getId).toList();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibraryRevealChoice(
                controllerId, revealedCards, matchingIds, false, false, false, true, false, 0, null,
                matchingCards.size(),
                "You may put any number of the revealed permanent cards onto the battlefield. "
                        + "The rest are put on the bottom of your library in a random order."));

        log.info("Game {} - {} resolving {} with required count {}, {} cards revealed, {} matching",
                gameData.id, playerName, entry.getCard().getName(), requiredCount,
                revealedCards.size(), matchingCards.size());
    }

    private void putRevealedCardsOnBottom(GameData gameData, UUID controllerId, List<Card> cards) {
        Collections.shuffle(cards);
        gameData.playerDecks.get(controllerId).addAll(cards);
    }
}
