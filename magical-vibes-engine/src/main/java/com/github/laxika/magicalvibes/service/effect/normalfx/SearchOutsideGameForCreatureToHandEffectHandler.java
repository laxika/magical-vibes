package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchOutsideGameForCreatureToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves Vivien's optional creature-card search from the controller's sideboard. */
@Component
@RequiredArgsConstructor
public class SearchOutsideGameForCreatureToHandEffectHandler implements NormalEffectHandlerBean {

    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SearchOutsideGameForCreatureToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> sideboard = gameData.playerSideboards.getOrDefault(controllerId, List.of());
        List<Card> creatures = sideboard.stream()
                .filter(card -> card.hasType(CardType.CREATURE))
                .toList();
        if (creatures.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(controllerId) + " finds no creature card outside the game."));
            return;
        }

        LibrarySearchParams params = LibrarySearchParams.builder(controllerId, new ArrayList<>(creatures))
                .reveals(true)
                .canFailToFind(true)
                .remainingCount(1)
                .destination(LibrarySearchDestination.HAND)
                .filterPredicate(new CardTypePredicate(CardType.CREATURE))
                .shuffleAfterSelection(false)
                .sourceSideboard(true)
                .build();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibrarySearch(
                params,
                "You may reveal a creature card you own from outside the game and put it into your hand.",
                true));
        gameLogService.append(gameData, GameLog.text(
                gameData.playerIdToName.get(controllerId) + " searches outside the game for a creature card."));
    }
}
