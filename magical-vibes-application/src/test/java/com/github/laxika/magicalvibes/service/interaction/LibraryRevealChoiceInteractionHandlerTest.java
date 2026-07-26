package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
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
class LibraryRevealChoiceInteractionHandlerTest {

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
        projectionSupport.register(new LibraryRevealChoiceInteractionHandler(
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

    @Test
    @DisplayName("begin sets LIBRARY_REVEAL_CHOICE and sends valid IDs/views with the begin-time maxCount and prompt")
    void beginSendsPrompt() {
        Card eligible = createCard("Grizzly Bears");
        Card ineligible = createCard("Shock");

        InteractionPromptMessage msg = projectionSupport.begin(
                gd, new PendingInteraction.LibraryRevealChoice(
                PLAYER1_ID, List.of(eligible, ineligible), List.of(eligible.getId()),
                true, false, false, false, false, 0, null, 1,
                "Choose any number of permanent cards with mana value 3 or less to put onto the battlefield."));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        assertThat(msg.cardIds()).containsExactly(eligible.getId());
        assertThat(msg.cards()).hasSize(1);
        assertThat(msg.maxCount()).isEqualTo(1);
        assertThat(msg.prompt())
                .isEqualTo("Choose any number of permanent cards with mana value 3 or less to put onto the battlefield.");
    }

    @Test
    @DisplayName("A null prompt sends no message at begin or replay (Karn Scion flows)")
    void nullPromptSendsNothing() {
        Card card = createCard("Silver Card");

        InteractionPromptMessage prompt = projectionSupport.begin(
                gd, new PendingInteraction.LibraryRevealChoice(
                PLAYER1_ID, List.of(card), List.of(card.getId()),
                false, true, false, false, false, 0, null, 1, null));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        assertThat(prompt).isNull();

        assertThat(registry.replayPrompt(gd, PLAYER1_ID)).isTrue();
    }

    @Test
    @DisplayName("dispatchAnswer delegates the chosen IDs to LibraryChoiceHandlerService")
    void dispatchDelegates() {
        Card card = createCard("Grizzly Bears");
        projectionSupport.begin(gd, new PendingInteraction.LibraryRevealChoice(
                PLAYER1_ID, List.of(card), List.of(card.getId()),
                false, true, false, false, false, 0, null, 1, "Choose."));
        Player player = new Player(PLAYER1_ID, "Player1");

        boolean handled = registry.dispatchAnswer(gd, player,
                new InteractionAnswer.CardsChosen(List.of(card.getId())));

        assertThat(handled).isTrue();
        verify(libraryChoiceHandlerService).handleLibraryRevealChoice(gd, player, List.of(card.getId()));
    }

    @Test
    @DisplayName("replayPrompt re-sends only to the decider")
    void replayOnlyToDecider() {
        Card card = createCard("Grizzly Bears");
        projectionSupport.begin(gd, new PendingInteraction.LibraryRevealChoice(
                PLAYER1_ID, List.of(card), List.of(card.getId()),
                false, true, false, false, false, 0, null, 1, "Choose."));
        assertThat(registry.replayPrompt(gd, PLAYER2_ID)).isTrue();

        assertThat(registry.replayPrompt(gd, PLAYER1_ID)).isTrue();
        assertThat(projectionSupport.projectedPrompt(gd))
                .isInstanceOf(InteractionPromptMessage.class);
    }
}
