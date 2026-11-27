package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibrarySearchFollowUp;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryAndGraveyardForCardsToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.library.LibrarySearchTriggerHelper;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link SearchLibraryAndGraveyardForCardsToHandEffect}. The library is searched once for every description
 * (CR 701.23h): the search is recorded as it begins, whatever it finds, unless a rule prevents it. A description with
 * no match among the cards the search can see in the library is taken from the graveyard; one with a library match is
 * a pick of that single search through {@link LibrarySearchSupport#startNextToHandPick}, which also offers the
 * graveyard's matches and shuffles once when the picks are done. With no pick, the library is shuffled here, also
 * when it couldn't be searched ("then shuffle", the Leonin Arbiter ruling).
 */
@Component
@RequiredArgsConstructor
public class SearchLibraryAndGraveyardForCardsToHandEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final LibrarySearchSupport librarySearchSupport;
    private final GraveyardService graveyardService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SearchLibraryAndGraveyardForCardsToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        SearchLibraryAndGraveyardForCardsToHandEffect search = (SearchLibraryAndGraveyardForCardsToHandEffect) effect;
        UUID controllerId = entry.getControllerId();
        String playerName = gameData.playerIdToName.get(controllerId);
        boolean librarySearched = !librarySearchSupport.isSearchPrevented(gameData, controllerId, false);
        List<Card> deck = gameData.playerDecks.get(controllerId);
        Set<UUID> searchableCardIds = Set.of();
        if (librarySearched && deck != null) {
            int limit = Math.min(librarySearchSupport.opponentSearchTopCardsLimit(gameData, controllerId),
                    deck.size());
            searchableCardIds = deck.subList(0, limit).stream().map(Card::getId).collect(Collectors.toSet());
        }
        if (librarySearched) {
            LibrarySearchTriggerHelper.recordSearchAndQueueTriggers(gameData, controllerId);
        }

        List<LibrarySearchFollowUp.ToHandPick> picks = new ArrayList<>();
        for (CardPredicate filter : search.cards()) {
            Set<UUID> searchable = searchableCardIds;
            boolean inLibrary = deck != null && deck.stream()
                    .anyMatch(card -> searchable.contains(card.getId()) && matches(gameData, controllerId, card, filter));
            if (inLibrary) {
                LibrarySearchFollowUp.ToHandPick pick = filter instanceof CardNamedPredicate named
                        ? LibrarySearchFollowUp.ToHandPick.ofName(named.cardName())
                        : LibrarySearchFollowUp.ToHandPick.of(filter, CardPredicateUtils.describeFilter(filter));
                picks.add(pick.alsoInGraveyard().withinSingleSearch(searchableCardIds));
                continue;
            }
            List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
            Optional<Card> graveyardMatch = graveyard == null ? Optional.empty()
                    : graveyard.stream().filter(card -> matches(gameData, controllerId, card, filter)).findFirst();
            graveyardMatch.ifPresent(found -> {
                graveyard.remove(found);
                graveyardService.notifyCardsLeftGraveyard(gameData, controllerId, found);
                graveyardService.addCardToHandFromGraveyard(gameData, controllerId, controllerId, found);
                gameLogService.append(gameData, GameLog.textCardText(
                        playerName + " searches their graveyard, reveals ", found, ", and puts it into their hand."));
            });
        }

        if (!picks.isEmpty()) {
            librarySearchSupport.startNextToHandPick(gameData, controllerId, LibrarySearchFollowUp.toHandPicks(picks));
            return;
        }
        if (deck != null) {
            LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
            gameLogService.append(gameData, GameLog.text(playerName + "'s library is shuffled."));
        }
    }

    private boolean matches(GameData gameData, UUID controllerId, Card card, CardPredicate filter) {
        return predicateEvaluationService.matchesCardPredicate(card, filter, null, gameData, controllerId);
    }
}
