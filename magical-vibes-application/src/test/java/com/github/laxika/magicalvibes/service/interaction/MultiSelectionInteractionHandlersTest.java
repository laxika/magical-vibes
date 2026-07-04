package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.ChooseMultipleCardsMessage;
import com.github.laxika.magicalvibes.networking.message.ChooseMultiplePermanentsMessage;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.service.input.GraveyardChoiceHandlerService;
import com.github.laxika.magicalvibes.service.input.MultiPermanentChoiceHandlerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class MultiSelectionInteractionHandlersTest {

    @Mock private SessionManager sessionManager;
    @Mock private CardViewFactory cardViewFactory;
    @Mock private MultiPermanentChoiceHandlerService multiPermanentChoiceHandlerService;
    @Mock private GraveyardChoiceHandlerService graveyardChoiceHandlerService;

    @Captor private ArgumentCaptor<Object> messageCaptor;

    private InteractionHandlerRegistry registry;
    private GameData gd;

    private static final UUID PLAYER1_ID = UUID.randomUUID();
    private static final UUID PLAYER2_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        registry = new InteractionHandlerRegistry();
        registry.register(new MultiPermanentChoiceInteractionHandler(sessionManager, multiPermanentChoiceHandlerService));
        registry.register(new MultiGraveyardChoiceInteractionHandler(sessionManager, cardViewFactory, graveyardChoiceHandlerService));

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

    @Nested
    @DisplayName("Multi-permanent choice")
    class MultiPermanent {

        @Test
        @DisplayName("begin sets state and sends the valid IDs with the begin-time prompt")
        void beginSendsPrompt() {
            UUID perm1 = UUID.randomUUID();
            UUID perm2 = UUID.randomUUID();

            registry.begin(gd, new PendingInteraction.MultiPermanentChoice(
                    PLAYER1_ID, List.of(perm1, perm2), 2, "Choose up to 2 permanents."));

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
            verify(sessionManager).sendToPlayer(eq(PLAYER1_ID), messageCaptor.capture());
            ChooseMultiplePermanentsMessage msg = (ChooseMultiplePermanentsMessage) messageCaptor.getValue();
            assertThat(msg.permanentIds()).containsExactly(perm1, perm2);
            assertThat(msg.maxCount()).isEqualTo(2);
            assertThat(msg.prompt()).isEqualTo("Choose up to 2 permanents.");
        }

        @Test
        @DisplayName("dispatchAnswer delegates the chosen permanents to MultiPermanentChoiceHandlerService")
        void dispatchDelegates() {
            UUID permId = UUID.randomUUID();
            registry.begin(gd, new PendingInteraction.MultiPermanentChoice(
                    PLAYER1_ID, List.of(permId), 1, "Pick one."));
            Player player = new Player(PLAYER1_ID, "Player1");

            boolean handled = registry.dispatchAnswer(gd, player,
                    new InteractionAnswer.PermanentsChosen(List.of(permId)));

            assertThat(handled).isTrue();
            verify(multiPermanentChoiceHandlerService).handleMultiplePermanentsChosen(gd, player, List.of(permId));
        }

        @Test
        @DisplayName("replayPrompt re-sends only to the decider")
        void replayOnlyToDecider() {
            UUID permId = UUID.randomUUID();
            registry.begin(gd, new PendingInteraction.MultiPermanentChoice(
                    PLAYER1_ID, List.of(permId), 1, "Pick one."));
            org.mockito.Mockito.clearInvocations(sessionManager);

            assertThat(registry.replayPrompt(gd, PLAYER2_ID)).isTrue();
            verifyNoInteractions(sessionManager);

            assertThat(registry.replayPrompt(gd, PLAYER1_ID)).isTrue();
            verify(sessionManager).sendToPlayer(eq(PLAYER1_ID), any(ChooseMultiplePermanentsMessage.class));
        }
    }

    @Nested
    @DisplayName("Multi-graveyard choice")
    class MultiGraveyard {

        @Test
        @DisplayName("begin sets state and sends IDs and views derived from the carried cards")
        void beginSendsPrompt() {
            Card card1 = createCard("Grizzly Bears");
            Card card2 = createCard("Serra Angel");

            registry.begin(gd, new PendingInteraction.MultiGraveyardChoice(
                    PLAYER1_ID, List.of(card1, card2), 2, "Choose up to 2 target creature cards from your graveyard."));

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
            verify(sessionManager).sendToPlayer(eq(PLAYER1_ID), messageCaptor.capture());
            ChooseMultipleCardsMessage msg = (ChooseMultipleCardsMessage) messageCaptor.getValue();
            assertThat(msg.cardIds()).containsExactly(card1.getId(), card2.getId());
            assertThat(msg.cards()).hasSize(2);
            assertThat(msg.maxCount()).isEqualTo(2);
            assertThat(msg.prompt()).isEqualTo("Choose up to 2 target creature cards from your graveyard.");
        }

        @Test
        @DisplayName("dispatchAnswer delegates the chosen cards to GraveyardChoiceHandlerService")
        void dispatchDelegates() {
            Card card = createCard("Grizzly Bears");
            registry.begin(gd, new PendingInteraction.MultiGraveyardChoice(
                    PLAYER1_ID, List.of(card), 1, "Choose."));
            Player player = new Player(PLAYER1_ID, "Player1");

            boolean handled = registry.dispatchAnswer(gd, player,
                    new InteractionAnswer.CardsChosen(List.of(card.getId())));

            assertThat(handled).isTrue();
            verify(graveyardChoiceHandlerService).handleMultipleCardsChosen(gd, player, List.of(card.getId()));
        }

        @Test
        @DisplayName("replayPrompt re-sends only to the decider")
        void replayOnlyToDecider() {
            Card card = createCard("Grizzly Bears");
            registry.begin(gd, new PendingInteraction.MultiGraveyardChoice(
                    PLAYER1_ID, List.of(card), 1, "Choose."));
            org.mockito.Mockito.clearInvocations(sessionManager);

            assertThat(registry.replayPrompt(gd, PLAYER2_ID)).isTrue();
            verifyNoInteractions(sessionManager);

            assertThat(registry.replayPrompt(gd, PLAYER1_ID)).isTrue();
            verify(sessionManager).sendToPlayer(eq(PLAYER1_ID), any(ChooseMultipleCardsMessage.class));
        }
    }
}
