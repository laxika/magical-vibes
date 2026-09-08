package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryAndOrGraveyardForCardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.library.LibrarySearchTriggerHelper;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves searches that may choose a matching card from either public or hidden zones. */
@Component
@RequiredArgsConstructor
public class SearchLibraryAndOrGraveyardForCardToHandEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GraveyardService graveyardService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final LibrarySearchSupport librarySearchSupport;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SearchLibraryAndOrGraveyardForCardToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        SearchLibraryAndOrGraveyardForCardToHandEffect search =
                (SearchLibraryAndOrGraveyardForCardToHandEffect) effect;
        UUID playerId = entry.getControllerId();
        boolean librarySearchAllowed = !librarySearchSupport.isSearchPrevented(gameData, playerId, false);

        List<Card> graveyard = gameData.playerGraveyards.getOrDefault(playerId, List.of());
        List<Card> graveyardMatches = graveyard.stream()
                .filter(card -> predicateEvaluationService.matchesCardPredicate(
                        card, search.filter(), null, gameData, playerId))
                .toList();

        List<Card> libraryMatches = List.of();
        List<Card> deck = gameData.playerDecks.get(playerId);
        if (librarySearchAllowed && deck != null) {
            int topLimit = librarySearchSupport.opponentSearchTopCardsLimit(gameData, playerId);
            libraryMatches = deck.stream()
                    .limit(Math.min(topLimit, deck.size()))
                    .filter(card -> predicateEvaluationService.matchesCardPredicate(
                            card, search.filter(), null, gameData, playerId))
                    .toList();
        }

        String playerName = gameData.playerIdToName.get(playerId);
        String cardLabel = CardPredicateUtils.describeFilter(search.filter());
        if (libraryMatches.isEmpty() && graveyardMatches.isEmpty()) {
            if (librarySearchAllowed) {
                LibrarySearchTriggerHelper.checkOpponentSearchTriggers(gameData, gameLogService, playerId);
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
                librarySearchAllowed, cardLabel));
        gameLogService.append(gameData, GameLog.text(playerName + " searches their library and/or graveyard."));
    }
}
