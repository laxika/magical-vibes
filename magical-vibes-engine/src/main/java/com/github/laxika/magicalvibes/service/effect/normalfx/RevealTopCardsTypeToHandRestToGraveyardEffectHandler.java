package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardsTypeToHandRestToGraveyardEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RevealTopCardsTypeToHandRestToGraveyardEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final LibraryRevealSupport libraryRevealSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealTopCardsTypeToHandRestToGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        RevealTopCardsTypeToHandRestToGraveyardEffect e = (RevealTopCardsTypeToHandRestToGraveyardEffect) effect;

        LibraryRevealSupport.TopCardsResult result = libraryRevealSupport.takeTopCardsFromLibrary(gameData, entry, e.count());
        if (result == null) return;
        UUID controllerId = result.controllerId();
        List<Card> topCards = result.topCards();
        String playerName = result.playerName();
        String cardName = entry.getCard().getName();

        List<Card> toHand = new ArrayList<>();
        List<Card> toGraveyard = new ArrayList<>();
        for (Card card : topCards) {
            if (LibraryRevealSupport.matchesCardTypes(card, e.cardTypes())) {
                toHand.add(card);
            } else {
                toGraveyard.add(card);
            }
        }

        // Broadcast the reveal with all card names
        String revealedNames = topCards.stream().map(Card::getName).reduce((a, b) -> a + ", " + b).orElse("");
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + " reveals " + revealedNames + " from the top of their library with " + cardName + "."));

        for (Card card : toHand) {
            gameData.addCardToHand(controllerId, card);
        }
        for (Card card : toGraveyard) {
            gameData.playerGraveyards.get(controllerId).add(card);
        }

        if (!toHand.isEmpty()) {
            String landNames = toHand.stream().map(Card::getName).reduce((a, b) -> a + ", " + b).orElse("");
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + " puts " + landNames + " into their hand."));
        }
        if (!toGraveyard.isEmpty()) {
            String restNames = toGraveyard.stream().map(Card::getName).reduce((a, b) -> a + ", " + b).orElse("");
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + " puts " + restNames + " into their graveyard."));
        }

        log.info("Game {} - {} resolving {} — {} to hand, {} to graveyard",
                gameData.id, playerName, cardName, toHand.size(), toGraveyard.size());
    
    }
}
