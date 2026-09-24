package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardsMatchingToGraveyardRestToBottomRandomEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class RevealTopCardsMatchingToGraveyardRestToBottomRandomEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final LibraryRevealSupport libraryRevealSupport;
    private final GraveyardService graveyardService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealTopCardsMatchingToGraveyardRestToBottomRandomEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        RevealTopCardsMatchingToGraveyardRestToBottomRandomEffect revealEffect =
                (RevealTopCardsMatchingToGraveyardRestToBottomRandomEffect) effect;
        LibraryRevealSupport.TopCardsResult result = libraryRevealSupport.takeTopCardsFromLibrary(
                gameData, entry, revealEffect.count());
        if (result == null) {
            return;
        }

        UUID controllerId = result.controllerId();
        List<Card> topCards = result.topCards();
        String playerName = result.playerName();
        String cardName = entry.getCard().getName();

        gameLogService.append(gameData, GameLog.text(playerName + " reveals "
                + topCards.stream().map(Card::getName).reduce((a, b) -> a + ", " + b).orElse("")
                + " from the top of their library with " + cardName + "."));

        List<Card> toBottom = new ArrayList<>();
        for (Card card : topCards) {
            if (revealEffect.matcher() != null
                    && predicateEvaluationService.matchesCardPredicate(
                    card, revealEffect.matcher(), null, gameData, controllerId)) {
                graveyardService.addCardToGraveyard(gameData, controllerId, card, Zone.LIBRARY);
            } else {
                toBottom.add(card);
            }
        }

        if (!toBottom.isEmpty()) {
            Collections.shuffle(toBottom);
            gameData.playerDecks.get(controllerId).addAll(toBottom);
        }
        gameLogService.append(gameData, GameLog.text(playerName + " puts " + toBottom.size()
                + " card(s) on the bottom of their library in a random order."));
        log.info("Game {} - {} puts {} revealed card(s) into the graveyard and {} on the library bottom",
                gameData.id, playerName, topCards.size() - toBottom.size(), toBottom.size());
    }
}
