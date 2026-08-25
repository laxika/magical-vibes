package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForDragonToGraveyardAndBecomeCopyUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SearchLibraryForDragonToGraveyardAndBecomeCopyUntilEndOfTurnEffectHandler
        implements NormalEffectHandlerBean {

    private static final CardPredicate DRAGON_PERMANENT = new CardAllOfPredicate(List.of(
            new CardSubtypePredicate(CardSubtype.DRAGON),
            new CardIsPermanentPredicate()));
    private static final String PROMPT =
            "Search your library for a Dragon permanent card and put it into your graveyard.";

    private final PredicateEvaluationService predicateEvaluationService;
    private final LibrarySearchSupport librarySearchSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SearchLibraryForDragonToGraveyardAndBecomeCopyUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (controllerId == null || sourcePermanentId == null) {
            return;
        }
        if (librarySearchSupport.isSearchPrevented(gameData, controllerId, true)) {
            return;
        }

        List<Card> deck = gameData.playerDecks.get(controllerId);
        List<Card> matchingCards = deck == null
                ? List.of()
                : deck.stream()
                        .filter(card -> predicateEvaluationService.matchesCardPredicate(
                                card, DRAGON_PERMANENT, null, gameData, controllerId))
                        .toList();

        if (matchingCards.isEmpty()
                && librarySearchSupport.librarySearchCastableCards(gameData, controllerId).isEmpty()) {
            librarySearchSupport.performLibrarySearch(
                    gameData, controllerId,
                    card -> predicateEvaluationService.matchesCardPredicate(
                            card, DRAGON_PERMANENT, null, gameData, controllerId),
                    "Dragon permanent cards", PROMPT, false, true,
                    LibrarySearchDestination.GRAVEYARD);
            return;
        }

        LibrarySearchParams params = LibrarySearchParams.builder(
                        controllerId, new ArrayList<>(matchingCards))
                .canFailToFind(true)
                .destination(LibrarySearchDestination.GRAVEYARD)
                .filterPredicate(DRAGON_PERMANENT)
                .sourcePermanentId(sourcePermanentId)
                .build();
        librarySearchSupport.sendLibrarySearchToPlayer(gameData, controllerId, params, PROMPT, true);
    }
}
