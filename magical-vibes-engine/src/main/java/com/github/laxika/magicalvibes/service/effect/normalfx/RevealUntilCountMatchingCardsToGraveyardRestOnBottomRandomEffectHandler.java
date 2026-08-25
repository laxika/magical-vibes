package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealUntilCountMatchingCardsToGraveyardRestOnBottomRandomEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/** Resolves a reveal-until-count effect that sends matches to the graveyard. */
@Slf4j
@Component
@RequiredArgsConstructor
public class RevealUntilCountMatchingCardsToGraveyardRestOnBottomRandomEffectHandler
        implements NormalEffectHandlerBean {

    private final AmountEvaluationService amountEvaluationService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;
    private final GraveyardService graveyardService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealUntilCountMatchingCardsToGraveyardRestOnBottomRandomEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var typedEffect = (RevealUntilCountMatchingCardsToGraveyardRestOnBottomRandomEffect) effect;
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

        List<Card> nonmatchingCards = new ArrayList<>(revealedCards);
        nonmatchingCards.removeAll(matchingCards);
        for (Card card : matchingCards) {
            graveyardService.addCardToGraveyard(gameData, controllerId, card, Zone.LIBRARY);
        }

        Collections.shuffle(nonmatchingCards);
        deck.addAll(nonmatchingCards);

        log.info("Game {} - {} reveals {} cards, {} matching cards put into the graveyard",
                gameData.id, playerName, revealedCards.size(), matchingCards.size());
    }
}
