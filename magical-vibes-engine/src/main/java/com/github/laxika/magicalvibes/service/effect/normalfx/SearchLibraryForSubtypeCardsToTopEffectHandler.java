package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForSubtypeCardsToTopEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link SearchLibraryForSubtypeCardsToTopEffect}: searches the controller's library for
 * the cards of the effect's subtype, holds them out of the library, and begins a
 * {@link PendingInteraction.SearchLibraryToTopChoice} letting the controller pick any number of
 * them to put on top (the rest are shuffled back into the library). Used by Goblin Recruiter.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SearchLibraryForSubtypeCardsToTopEffectHandler implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;
    private final GameBroadcastService gameBroadcastService;
    private final LibrarySearchSupport librarySearchSupport;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SearchLibraryForSubtypeCardsToTopEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        if (librarySearchSupport.isSearchPrevented(gameData, controllerId)) return;

        String label = ((SearchLibraryForSubtypeCardsToTopEffect) effect).subtype().getDisplayName();
        String playerName = gameData.playerIdToName.get(controllerId);

        List<Card> deck = gameData.playerDecks.get(controllerId);
        if (deck == null || deck.isEmpty()) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + " searches their library but it is empty. Library is shuffled."));
            return;
        }

        CardSubtypePredicate filter =
                new CardSubtypePredicate(((SearchLibraryForSubtypeCardsToTopEffect) effect).subtype());
        List<Card> matching = deck.stream()
                .filter(c -> predicateEvaluationService.matchesCardPredicate(c, filter, null, gameData, controllerId))
                .toList();

        if (matching.isEmpty()) {
            LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + " searches their library but finds no " + label + " cards. Library is shuffled."));
            log.info("Game {} - {} searches library, no {} cards found", gameData.id, playerName, label);
            return;
        }

        // Hold the matching cards out of the library; the choice interaction decides which go on top
        // and returns the rest before the library is shuffled.
        List<Card> pool = new ArrayList<>(matching);
        Set<UUID> poolIds = pool.stream().map(Card::getId).collect(Collectors.toSet());
        deck.removeIf(c -> poolIds.contains(c.getId()));

        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.SearchLibraryToTopChoice(controllerId, pool, label));

        log.info("Game {} - {} searches library for {} cards to put on top ({} matches)",
                gameData.id, playerName, label, pool.size());
    }
}
