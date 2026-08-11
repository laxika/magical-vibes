package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCardsToTopEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.service.GameLogService;
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
 * Resolves {@link SearchLibraryForCardsToTopEffect} by reusing the multi-card search-to-top choice
 * used by Goblin Recruiter.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SearchLibraryForCardsToTopEffectHandler implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;
    private final LibrarySearchSupport librarySearchSupport;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SearchLibraryForCardsToTopEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        if (librarySearchSupport.isSearchPrevented(gameData, controllerId)) {
            return;
        }

        CardPredicate filter = ((SearchLibraryForCardsToTopEffect) effect).filter();
        String description = CardPredicateUtils.describeFilter(filter);
        String label = description.replaceFirst("\\s+card$", "");
        String playerName = gameData.playerIdToName.get(controllerId);

        List<Card> deck = gameData.playerDecks.get(controllerId);
        if (deck == null || deck.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(playerName + " searches their library but it is empty. Library is shuffled."));
            return;
        }

        List<Card> matching = deck.stream()
                .filter(card -> predicateEvaluationService.matchesCardPredicate(card, filter, null, gameData, controllerId))
                .toList();

        if (matching.isEmpty()) {
            LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
            gameLogService.append(gameData, GameLog.text(playerName + " searches their library but finds no "
                    + label + " cards. Library is shuffled."));
            log.info("Game {} - {} searches library, no {} cards found", gameData.id, playerName, label);
            return;
        }

        List<Card> pool = new ArrayList<>(matching);
        Set<UUID> poolIds = pool.stream().map(Card::getId).collect(Collectors.toSet());
        deck.removeIf(card -> poolIds.contains(card.getId()));

        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.SearchLibraryToTopChoice(controllerId, pool, label));

        log.info("Game {} - {} searches library for {} cards to put on top ({} matches)",
                gameData.id, playerName, label, pool.size());
    }
}
