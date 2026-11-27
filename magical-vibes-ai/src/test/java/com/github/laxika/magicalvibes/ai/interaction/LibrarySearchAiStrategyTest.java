package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.ai.AiGameActions;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.effect.CastFromLibraryWhileSearchingEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class LibrarySearchAiStrategyTest {

    private final LibrarySearchAiStrategy strategy = new LibrarySearchAiStrategy();

    private UUID aiPlayerId;
    private GameData gameData;
    private AiGameActions gameActions;
    private AiInteractionContext context;

    @BeforeEach
    void setUp() {
        aiPlayerId = UUID.randomUUID();
        gameData = new GameData(UUID.randomUUID(), "library-search-ai", aiPlayerId, "AI");
        gameActions = mock(AiGameActions.class);
        context = new AiInteractionContext(
                gameData, gameData.id, aiPlayerId, mock(GameQueryService.class), gameActions);
    }

    @Test
    @DisplayName("Fails to find when only an optional cast from the library is offered")
    void failsToFindWhenOnlyAnOptionalLibraryCastIsOffered() throws Exception {
        Card castOffer = librarySearchCastOffer("Cast offer", "{5}{G}{G}");

        strategy.answer(optionalSearch(List.of(castOffer)), context);

        verify(gameActions).answerInteraction(new InteractionAnswer.LibraryCardChosen(-1));
    }

    @Test
    @DisplayName("Takes a search result over a higher-value optional cast from the library")
    void choosesSearchResultInsteadOfHigherValueOptionalLibraryCast() throws Exception {
        Card searchResult = card("Search result", CardType.LAND, null);
        Card castOffer = librarySearchCastOffer("Cast offer", "{5}{G}{G}");

        strategy.answer(optionalSearch(List.of(searchResult, castOffer)), context);

        verify(gameActions).answerInteraction(new InteractionAnswer.LibraryCardChosen(0));
    }

    @Test
    @DisplayName("Answers an opponent's search when the AI controls Opposition Agent and so chooses the find")
    void answersAsOppositionAgentController() throws Exception {
        UUID searcherId = UUID.randomUUID();
        Card land = card("Land", CardType.LAND, null);
        Card creature = card("Creature", CardType.CREATURE, "{3}");
        LibrarySearchParams params = LibrarySearchParams.builder(searcherId, List.of(land, creature))
                .canFailToFind(true)
                .decisionPlayerId(aiPlayerId)
                .build();

        strategy.answer(new PendingInteraction.LibrarySearch(params, "Search their library.", true), context);

        verify(gameActions).answerInteraction(new InteractionAnswer.LibraryCardChosen(1));
    }

    @Test
    @DisplayName("Leaves a search to the Opposition Agent controller when the AI is only the searcher")
    void leavesTheSearchToTheOppositionAgentController() throws Exception {
        Card land = card("Land", CardType.LAND, null);
        LibrarySearchParams params = LibrarySearchParams.builder(aiPlayerId, List.of(land))
                .canFailToFind(true)
                .decisionPlayerId(UUID.randomUUID())
                .build();

        strategy.answer(new PendingInteraction.LibrarySearch(params, "Search your library.", true), context);

        verify(gameActions, never()).answerInteraction(any());
    }

    private PendingInteraction.LibrarySearch optionalSearch(List<Card> cards) {
        LibrarySearchParams params = LibrarySearchParams.builder(aiPlayerId, cards)
                .canFailToFind(true)
                .allowCastFromLibraryWhileSearching(true)
                .build();
        return new PendingInteraction.LibrarySearch(params, "Search your library.", true);
    }

    private static Card librarySearchCastOffer(String name, String manaCost) {
        Card card = card(name, CardType.CREATURE, manaCost);
        card.addStaticEffect(new CastFromLibraryWhileSearchingEffect());
        return card;
    }

    private static Card card(String name, CardType type, String manaCost) {
        Card card = new Card();
        card.setName(name);
        card.setType(type);
        card.setManaCost(manaCost);
        return card;
    }
}
