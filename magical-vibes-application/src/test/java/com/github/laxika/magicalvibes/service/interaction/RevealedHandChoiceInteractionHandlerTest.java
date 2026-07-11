package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.ChooseFromRevealedHandMessage;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.service.input.CardChoiceHandlerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RevealedHandChoiceInteractionHandlerTest {

    @Mock private SessionManager sessionManager;
    @Mock private CardViewFactory cardViewFactory;
    @Mock private CardChoiceHandlerService cardChoiceHandlerService;

    @Captor private ArgumentCaptor<Object> messageCaptor;

    private InteractionHandlerRegistry registry;
    private GameData gd;

    private static final UUID PLAYER1_ID = UUID.randomUUID();
    private static final UUID PLAYER2_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        registry = new InteractionHandlerRegistry();
        registry.register(new RevealedHandChoiceInteractionHandler(
                sessionManager, cardViewFactory, cardChoiceHandlerService));

        gd = new GameData(UUID.randomUUID(), "test-game", PLAYER1_ID, "Player1");
        gd.playerIds.addAll(List.of(PLAYER1_ID, PLAYER2_ID));
        gd.orderedPlayerIds.addAll(List.of(PLAYER1_ID, PLAYER2_ID));
        gd.playerIdToName.put(PLAYER1_ID, "Player1");
        gd.playerIdToName.put(PLAYER2_ID, "Player2");
        gd.playerHands.put(PLAYER1_ID, new ArrayList<>());
        gd.playerHands.put(PLAYER2_ID, new ArrayList<>());
    }

    private static Card createCard(String name) {
        Card card = new Card();
        card.setName(name);
        return card;
    }

    private PendingInteraction.RevealedHandChoice choice(List<Integer> validIndices, String prompt) {
        return new PendingInteraction.RevealedHandChoice(
                PLAYER1_ID, PLAYER2_ID, validIndices, 1, false, false, List.of(), null, prompt, false, false);
    }

    @Test
    @DisplayName("begin sets REVEALED_HAND_CHOICE state and sends the target's hand views with the begin-time prompt")
    void beginSendsPrompt() {
        Card card1 = createCard("Bear");
        Card card2 = createCard("Wolf");
        gd.playerHands.get(PLAYER2_ID).addAll(List.of(card1, card2));
        CardView view1 = mock(CardView.class);
        CardView view2 = mock(CardView.class);
        when(cardViewFactory.create(card1)).thenReturn(view1);
        when(cardViewFactory.create(card2)).thenReturn(view2);

        registry.begin(gd, choice(List.of(0, 1), "Choose a card to put on top of Player2's library."));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.RevealedHandChoice.class);
        verify(sessionManager).sendToPlayer(eq(PLAYER1_ID), messageCaptor.capture());
        ChooseFromRevealedHandMessage msg = (ChooseFromRevealedHandMessage) messageCaptor.getValue();
        assertThat(msg.cards()).containsExactly(view1, view2);
        assertThat(msg.validIndices()).containsExactly(0, 1);
        assertThat(msg.prompt()).isEqualTo("Choose a card to put on top of Player2's library.");
    }

    @Test
    @DisplayName("dispatchAnswer delegates the chosen index to CardChoiceHandlerService")
    void dispatchDelegates() {
        Card card = createCard("Bear");
        gd.playerHands.get(PLAYER2_ID).add(card);
        when(cardViewFactory.create(card)).thenReturn(mock(CardView.class));
        registry.begin(gd, choice(List.of(0), "Pick one."));
        Player player = new Player(PLAYER1_ID, "Player1");

        boolean handled = registry.dispatchAnswer(gd, player, new InteractionAnswer.CardIndexChosen(0));

        assertThat(handled).isTrue();
        verify(cardChoiceHandlerService).handleRevealedHandCardChosen(gd, player, 0);
    }

    @Test
    @DisplayName("replayPrompt re-sends only to the decider")
    void replayOnlyToDecider() {
        Card card = createCard("Bear");
        gd.playerHands.get(PLAYER2_ID).add(card);
        when(cardViewFactory.create(card)).thenReturn(mock(CardView.class));
        registry.begin(gd, choice(List.of(0), "Pick one."));
        org.mockito.Mockito.clearInvocations(sessionManager);

        assertThat(registry.replayPrompt(gd, PLAYER2_ID)).isTrue();
        verifyNoInteractions(sessionManager);

        assertThat(registry.replayPrompt(gd, PLAYER1_ID)).isTrue();
        verify(sessionManager).sendToPlayer(eq(PLAYER1_ID), any(ChooseFromRevealedHandMessage.class));
    }
}
