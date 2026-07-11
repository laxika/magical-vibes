package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealUntilNonlandCardsToHandRestToBottomEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RevealUntilNonlandCardsToHandRestToBottomEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final LibraryRevealSupport libraryRevealSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealUntilNonlandCardsToHandRestToBottomEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        RevealUntilNonlandCardsToHandRestToBottomEffect e = (RevealUntilNonlandCardsToHandRestToBottomEffect) effect;

        UUID controllerId = entry.getControllerId();
        String playerName = gameData.playerIdToName.get(controllerId);
        String cardName = entry.getCard().getName();
        List<Card> deck = gameData.playerDecks.get(controllerId);

        // Reveal from the top until we have revealed the required number of nonland cards
        // (or the library runs out).
        List<Card> revealed = new ArrayList<>();
        List<Card> toHand = new ArrayList<>();
        List<Card> rest = new ArrayList<>();
        while (!deck.isEmpty() && toHand.size() < e.count()) {
            Card card = deck.removeFirst();
            revealed.add(card);
            if (card.hasType(CardType.LAND)) {
                rest.add(card);
            } else {
                toHand.add(card);
            }
        }

        if (revealed.isEmpty()) {
            gameBroadcastService.logAndBroadcast(gameData,
                    playerName + "'s library is empty — no cards are revealed with " + cardName + ".");
            return;
        }

        String revealedNames = revealed.stream().map(Card::getName).reduce((a, b) -> a + ", " + b).orElse("");
        gameBroadcastService.logAndBroadcast(gameData,
                playerName + " reveals " + revealedNames + " from the top of their library with " + cardName + ".");

        for (Card card : toHand) {
            gameData.addCardToHand(controllerId, card);
        }
        if (!toHand.isEmpty()) {
            String handNames = toHand.stream().map(Card::getName).reduce((a, b) -> a + ", " + b).orElse("");
            gameBroadcastService.logAndBroadcast(gameData,
                    playerName + " puts " + handNames + " into their hand.");
        }

        log.info("Game {} - {} resolving {} — {} nonland to hand, {} lands to bottom",
                gameData.id, playerName, cardName, toHand.size(), rest.size());

        // Put the rest (lands) on the bottom of the library in any order.
        if (!rest.isEmpty()) {
            libraryRevealSupport.reorderRemainingToBottom(gameData, controllerId, rest);
        }
    }
}
