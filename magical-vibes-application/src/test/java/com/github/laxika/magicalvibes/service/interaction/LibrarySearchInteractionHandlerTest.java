package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.InteractionPromptMessage;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.service.input.LibraryChoiceHandlerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LibrarySearchInteractionHandlerTest {

    @Mock private CardViewFactory cardViewFactory;
    @Mock private LibraryChoiceHandlerService libraryChoiceHandlerService;

    private InteractionHandlerRegistry registry;
    private InteractionProjectionTestSupport projectionSupport;
    private GameData gd;

    private static final UUID PLAYER1_ID = UUID.randomUUID();
    private static final UUID PLAYER2_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        projectionSupport = new InteractionProjectionTestSupport(cardViewFactory);
        registry = projectionSupport.registry();
        projectionSupport.register(new LibrarySearchInteractionHandler(
                libraryChoiceHandlerService));

        gd = new GameData(UUID.randomUUID(), "test-game", PLAYER1_ID, "Player1");
        gd.playerIds.addAll(List.of(PLAYER1_ID, PLAYER2_ID));
        gd.orderedPlayerIds.addAll(List.of(PLAYER1_ID, PLAYER2_ID));
        gd.playerIdToName.put(PLAYER1_ID, "Player1");
        gd.playerIdToName.put(PLAYER2_ID, "Player2");

        lenient().when(cardViewFactory.create(any(Card.class))).thenReturn(mock(CardView.class));
    }

    private static Card createCard(String name) {
        Card card = new Card();
        card.setName(name);
        return card;
    }

    private PendingInteraction.LibrarySearch search(List<Card> cards, String messagePrompt, boolean canFail) {
        return new PendingInteraction.LibrarySearch(
                LibrarySearchParams.builder(PLAYER1_ID, cards)
                        .canFailToFind(canFail)
                        .destination(LibrarySearchDestination.HAND)
                        .build(),
                messagePrompt, canFail);
    }

    @Test
    @DisplayName("begin sets LIBRARY_SEARCH and sends the presented cards with the begin-time message prompt and flag")
    void beginSendsPrompt() {
        List<Card> cards = List.of(createCard("Forest"), createCard("Island"));

        InteractionPromptMessage msg = projectionSupport.begin(
                gd,
                search(
                        cards,
                        "Search your library for a basic land card to put into your hand.",
                        true));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(msg.cards()).hasSize(2);
        assertThat(msg.prompt()).isEqualTo("Search your library for a basic land card to put into your hand.");
        assertThat(msg.declinable()).isTrue();
    }

    @Test
    @DisplayName("dispatchAnswer delegates the chosen index to LibraryChoiceHandlerService")
    void dispatchDelegates() {
        projectionSupport.begin(
                gd, search(List.of(createCard("Forest")), "Search.", false));
        Player player = new Player(PLAYER1_ID, "Player1");

        boolean handled = registry.dispatchAnswer(gd, player, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(handled).isTrue();
        verify(libraryChoiceHandlerService).handleLibraryCardChosen(gd, player, 0);
    }

}
