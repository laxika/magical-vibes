package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryAndOrGraveyardForCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.library.LibrarySearchTriggerHelper;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Resolves multi-card searches that may use either the controller's library or graveyard. */
@Component
@RequiredArgsConstructor
@Slf4j
public class SearchLibraryAndOrGraveyardForCardsEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final LibrarySearchSupport librarySearchSupport;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SearchLibraryAndOrGraveyardForCardsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        SearchLibraryAndOrGraveyardForCardsEffect search =
                (SearchLibraryAndOrGraveyardForCardsEffect) effect;
        UUID playerId = entry.getControllerId();
        boolean librarySearchAllowed = !librarySearchSupport.isSearchPrevented(gameData, playerId, false);

        List<Card> graveyard = gameData.playerGraveyards.getOrDefault(playerId, List.of());
        List<Card> graveyardMatches = graveyard.stream()
                .filter(card -> matches(card, search, Zone.GRAVEYARD, gameData, playerId))
                .toList();

        List<Card> libraryMatches = List.of();
        List<Card> deck = gameData.playerDecks.get(playerId);
        if (librarySearchAllowed && deck != null) {
            int topLimit = librarySearchSupport.opponentSearchTopCardsLimit(gameData, playerId);
            libraryMatches = deck.stream()
                    .limit(Math.min(topLimit, deck.size()))
                    .filter(card -> matches(card, search, Zone.LIBRARY, gameData, playerId))
                    .toList();
        }

        String playerName = gameData.playerIdToName.get(playerId);
        String cardLabel = CardPredicateUtils.describeFilter(search.filter());
        if (libraryMatches.isEmpty() && graveyardMatches.isEmpty()) {
            if (librarySearchAllowed) {
                LibrarySearchTriggerHelper.recordSearchAndQueueTriggers(gameData, gameLogService, playerId);
                if (deck != null) {
                    LibraryShuffleHelper.shuffleLibrary(gameData, playerId);
                }
                gameLogService.append(gameData, GameLog.text(playerName + " searches their library and graveyard but finds no "
                        + cardLabel + "." + (deck == null ? "" : " Library is shuffled.")));
            }
            return;
        }

        List<Card> pool = new ArrayList<>(libraryMatches);
        pool.addAll(graveyardMatches);
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.SearchLibraryAndOrGraveyardChoice(
                playerId, pool, new HashSet<>(libraryMatches.stream().map(Card::getId).toList()),
                librarySearchAllowed, cardLabel, search.destination(), search.maxCount()));
        gameLogService.append(gameData, GameLog.text(playerName + " searches their library and/or graveyard."));
        log.info("Game {} - {} searches library and/or graveyard for up to {} card(s)",
                gameData.id, playerName, search.maxCount());
    }

    private boolean matches(Card card, SearchLibraryAndOrGraveyardForCardsEffect search, Zone zone,
                            GameData gameData, UUID playerId) {
        return predicateEvaluationService.matchesCardPredicate(card, search.filter(), null, gameData, playerId)
                && (search.destination() != LibrarySearchDestination.BATTLEFIELD
                || !gameQueryService.isCardBlockedFromEnteringFromZone(gameData, card, zone));
    }
}
