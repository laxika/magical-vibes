package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EcologicalAppreciationEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.library.LibrarySearchTriggerHelper;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves Ecological Appreciation's X-limited creature search. */
@Slf4j
@Component
@RequiredArgsConstructor
public class EcologicalAppreciationEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final LibrarySearchSupport librarySearchSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EcologicalAppreciationEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        int maxManaValue = entry.getXValue();
        boolean librarySearchPrevented = librarySearchSupport.isSearchPrevented(gameData, controllerId);

        List<Card> pool = new ArrayList<>();
        if (!librarySearchPrevented) {
            LibrarySearchTriggerHelper.checkOpponentSearchTriggers(gameData, gameLogService, controllerId);
            gameData.playersWhoSearchedLibraryThisTurn.add(controllerId);
            addMatchingCards(pool, gameData.playerDecks.get(controllerId), maxManaValue);
        }
        addMatchingCards(pool, gameData.playerGraveyards.get(controllerId), maxManaValue);

        if (pool.isEmpty()) {
            if (!librarySearchPrevented) {
                LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
            }
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(controllerId)
                            + " finds no qualifying creature cards for Ecological Appreciation."));
            return;
        }

        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.EcologicalAppreciationSearchChoice(
                        controllerId, pool, maxManaValue));
        log.info("Game {} - Awaiting {} to choose Ecological Appreciation cards from a pool of {}",
                gameData.id, gameData.playerIdToName.get(controllerId), pool.size());
    }

    private void addMatchingCards(List<Card> pool, List<Card> cards, int maxManaValue) {
        if (cards == null) {
            return;
        }
        cards.stream()
                .filter(card -> card.hasType(CardType.CREATURE))
                .filter(card -> card.getManaValue() <= maxManaValue)
                .forEach(pool::add);
    }
}
