package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AetherworksMarvelEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class AetherworksMarvelEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AetherworksMarvelEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        AetherworksMarvelEffect aetherworksEffect = (AetherworksMarvelEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        if (deck == null || deck.isEmpty()) {
            return;
        }

        int count = Math.min(Math.max(0, aetherworksEffect.lookCount()), deck.size());
        if (count == 0) {
            return;
        }
        List<Card> topCards = LibraryRevealSupport.takeTopCards(deck, count);
        gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(controllerId)
                + " looks at the top " + count + " cards of their library ("
                + entry.getCard().getName() + ")."));

        List<Card> castable = topCards.stream()
                .filter(card -> !card.hasType(CardType.LAND))
                .toList();
        if (castable.isEmpty()) {
            Collections.shuffle(topCards);
            deck.addAll(topCards);
            return;
        }

        String prompt = "You may cast a spell from among the top " + count
                + " cards without paying its mana cost.";
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibrarySearch(
                LibrarySearchParams.builder(controllerId, castable)
                        .reveals(false)
                        .canFailToFind(true)
                        .sourceCards(new ArrayList<>(topCards))
                        .reorderRemainingToBottom(true)
                        .shuffleAfterSelection(false)
                        .prompt(prompt)
                        .destination(LibrarySearchDestination.CAST_WITHOUT_PAYING)
                        .build(),
                prompt,
                true));
        log.info("Game {} - {} looks at the top {} cards for Aetherworks Marvel",
                gameData.id, gameData.playerIdToName.get(controllerId), count);
    }
}
