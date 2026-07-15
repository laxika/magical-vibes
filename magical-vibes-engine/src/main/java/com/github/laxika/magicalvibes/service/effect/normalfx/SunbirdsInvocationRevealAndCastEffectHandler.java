package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SunbirdsInvocationRevealAndCastEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SunbirdsInvocationRevealAndCastEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SunbirdsInvocationRevealAndCastEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        SunbirdsInvocationRevealAndCastEffect e = (SunbirdsInvocationRevealAndCastEffect) effect;

        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);
        String sourceName = entry.getCard().getName();
        int manaValue = e.manaValue();

        if (manaValue <= 0 || deck.isEmpty()) {
            String logMsg = manaValue <= 0
                    ? sourceName + ": spell has mana value 0 — no cards revealed."
                    : sourceName + ": " + playerName + "'s library is empty.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logMsg));
            return;
        }

        int count = Math.min(manaValue, deck.size());
        List<Card> topCards = LibraryRevealSupport.takeTopCards(deck, count);

        // Reveal all cards
        String revealedNames = topCards.stream().map(Card::getName).reduce((a, b) -> a + ", " + b).orElse("");
        String revealLog = playerName + " reveals " + revealedNames + " (" + sourceName + ").";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(revealLog));
        log.info("Game {} - {} reveals {} cards for Sunbird's Invocation (MV {})",
                gameData.id, playerName, count, manaValue);

        // Filter to non-land cards with mana value ≤ X
        List<Card> castable = topCards.stream()
                .filter(c -> !c.hasType(CardType.LAND))
                .filter(c -> c.getManaValue() <= manaValue)
                .toList();

        if (castable.isEmpty()) {
            // No castable cards — put all to bottom in random order
            Collections.shuffle(topCards);
            deck.addAll(topCards);
            String noMatchLog = sourceName + " — no castable cards found. Cards are put on the bottom in a random order.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(noMatchLog));
            log.info("Game {} - Sunbird's Invocation: no castable cards, {} to bottom", gameData.id, count);
            return;
        }

        // Present choice to controller
        String prompt = "You may cast a spell with mana value " + manaValue
                + " or less from among the revealed cards without paying its mana cost.";

        interactionHandlerRegistry.begin(gameData, new com.github.laxika.magicalvibes.model.PendingInteraction.LibrarySearch(
                LibrarySearchParams.builder(controllerId, castable)
                .reveals(true)
                .canFailToFind(true)
                .sourceCards(new ArrayList<>(topCards))
                .reorderRemainingToBottom(true)
                .shuffleAfterSelection(false)
                .prompt(prompt)
                .destination(LibrarySearchDestination.CAST_WITHOUT_PAYING)
                .build(),
                prompt,
                true));
    
    }
}
