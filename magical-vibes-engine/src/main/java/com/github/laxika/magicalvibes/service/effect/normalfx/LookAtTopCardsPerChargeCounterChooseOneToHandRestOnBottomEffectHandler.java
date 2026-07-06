package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsPerChargeCounterChooseOneToHandRestOnBottomEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LookAtTopCardsPerChargeCounterChooseOneToHandRestOnBottomEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LookAtTopCardsPerChargeCounterChooseOneToHandRestOnBottomEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {

        int count = entry.getXValue();
        UUID controllerId = entry.getControllerId();
        String playerName = gameData.playerIdToName.get(controllerId);

        if (count <= 0) {
            String logMsg = entry.getCard().getName() + ": no charge counters, nothing to look at.";
            gameBroadcastService.logAndBroadcast(gameData, logMsg);
            return;
        }

        List<Card> deck = gameData.playerDecks.get(controllerId);
        int actual = Math.min(count, deck.size());
        if (actual == 0) {
            String logMsg = entry.getCard().getName() + ": " + playerName + "'s library is empty.";
            gameBroadcastService.logAndBroadcast(gameData, logMsg);
            return;
        }

        List<Card> topCards = LibraryRevealSupport.takeTopCards(deck, actual);

        if (actual == 1) {
            gameData.addCardToHand(controllerId, topCards.getFirst());
            String logMsg = playerName + " looks at the top card of their library and puts it into their hand.";
            gameBroadcastService.logAndBroadcast(gameData, logMsg);
            return;
        }

        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.HandTopBottomChoice(controllerId, topCards));

        gameBroadcastService.logAndBroadcast(gameData,
                playerName + " looks at the top " + LibraryRevealSupport.pluralCards(actual) + " of their library.");
        log.info("Game {} - {} resolving {} with {} cards", gameData.id, playerName, entry.getCard().getName(), actual);
    
    }
}
