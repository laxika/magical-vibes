package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealUntilCardPredicateToHandRestToGraveyardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class RevealUntilCardPredicateToHandRestToGraveyardEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GraveyardService graveyardService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealUntilCardPredicateToHandRestToGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var typedEffect = (RevealUntilCardPredicateToHandRestToGraveyardEffect) effect;
        UUID controllerId = entry.getControllerId();
        String playerName = gameData.playerIdToName.get(controllerId);
        List<Card> deck = gameData.playerDecks.get(controllerId);

        if (deck == null || deck.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    playerName + "'s library is empty — no cards are revealed."));
            return;
        }

        List<Card> revealedCards = new ArrayList<>();
        Card foundCard = null;
        while (!deck.isEmpty()) {
            Card card = deck.removeFirst();
            revealedCards.add(card);
            if (predicateEvaluationService.matchesCardPredicate(
                    card, typedEffect.predicate(), entry.getCard().getId(), gameData, controllerId)) {
                foundCard = card;
                break;
            }
        }

        String revealedNames = revealedCards.stream()
                .map(Card::getName)
                .collect(Collectors.joining(", "));
        gameLogService.append(gameData, GameLog.text(
                playerName + " reveals " + revealedNames + " from the top of their library."));

        if (foundCard != null) {
            revealedCards.remove(foundCard);
            gameData.addCardToHand(controllerId, foundCard);
            gameLogService.append(gameData, GameLog.text(
                    playerName + " puts " + foundCard.getName() + " into their hand."));
        } else {
            gameLogService.append(gameData, GameLog.text(
                    playerName + " reveals their entire library — no matching card was found."));
        }

        for (Card card : revealedCards) {
            graveyardService.addCardToGraveyard(gameData, controllerId, card, Zone.LIBRARY);
        }

        log.info("Game {} - {} reveals {} cards, matching card found={}",
                gameData.id, playerName, revealedCards.size() + (foundCard != null ? 1 : 0),
                foundCard != null ? foundCard.getName() : "none");
    }
}
