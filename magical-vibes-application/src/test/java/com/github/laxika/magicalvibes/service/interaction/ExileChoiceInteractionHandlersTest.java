package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingKnowledgePoolCast;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.ChooseMultipleCardsMessage;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.service.effect.normalfx.ExileSupport;
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
class ExileChoiceInteractionHandlersTest {

    @Mock private SessionManager sessionManager;
    @Mock private CardViewFactory cardViewFactory;
    @Mock private ExileSupport exileSupport;

    @Captor private ArgumentCaptor<Object> messageCaptor;

    private InteractionHandlerRegistry registry;
    private GameData gd;

    private static final UUID PLAYER1_ID = UUID.randomUUID();
    private static final UUID PLAYER2_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        registry = new InteractionHandlerRegistry();
        registry.register(new KnowledgePoolCastChoiceInteractionHandler(sessionManager, cardViewFactory, exileSupport));
        registry.register(new MirrorOfFateChoiceInteractionHandler(sessionManager, cardViewFactory, exileSupport));

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
    @DisplayName("Knowledge Pool cast choice")
    class KnowledgePool {

        @Test
        @DisplayName("begin sets state and sends the pool cards with maxCount 1")
        void beginSendsPrompt() {
            UUID kpPermanentId = UUID.randomUUID();
            Card poolCard = createCard("Doom Blade");
            gd.addToExile(PLAYER2_ID, poolCard, kpPermanentId);
            gd.queueInteraction(new PendingKnowledgePoolCast(kpPermanentId));

            registry.begin(gd, new PendingInteraction.KnowledgePoolCastChoice(
                    PLAYER1_ID, List.of(poolCard.getId()), 1));

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.KnowledgePoolCastChoice.class);
            verify(sessionManager).sendToPlayer(eq(PLAYER1_ID), messageCaptor.capture());
            ChooseMultipleCardsMessage msg = (ChooseMultipleCardsMessage) messageCaptor.getValue();
            assertThat(msg.cardIds()).containsExactly(poolCard.getId());
            assertThat(msg.maxCount()).isEqualTo(1);
            assertThat(msg.prompt()).contains("Knowledge Pool");
        }

        @Test
        @DisplayName("dispatchAnswer delegates the chosen cards to ExileSupport")
        void dispatchDelegates() {
            UUID cardId = UUID.randomUUID();
            registry.begin(gd, new PendingInteraction.KnowledgePoolCastChoice(PLAYER1_ID, List.of(cardId), 1));
            Player player = new Player(PLAYER1_ID, "Player1");

            boolean handled = registry.dispatchAnswer(gd, player, new InteractionAnswer.CardsChosen(List.of(cardId)));

            assertThat(handled).isTrue();
            verify(exileSupport).handleKnowledgePoolCastChoice(gd, player, List.of(cardId));
        }
    }

    @Nested
    @DisplayName("Mirror of Fate choice")
    class MirrorOfFate {

        @Test
        @DisplayName("begin sets state and sends the exiled cards with the given maxCount")
        void beginSendsPrompt() {
            Card exiled = createCard("Lightning Bolt");
            gd.addToExile(PLAYER1_ID, exiled);

            registry.begin(gd, new PendingInteraction.MirrorOfFateChoice(
                    PLAYER1_ID, List.of(exiled.getId()), 7));

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MirrorOfFateChoice.class);
            verify(sessionManager).sendToPlayer(eq(PLAYER1_ID), messageCaptor.capture());
            ChooseMultipleCardsMessage msg = (ChooseMultipleCardsMessage) messageCaptor.getValue();
            assertThat(msg.cardIds()).containsExactly(exiled.getId());
            assertThat(msg.maxCount()).isEqualTo(7);
            assertThat(msg.prompt()).contains("seven");
        }

        @Test
        @DisplayName("dispatchAnswer delegates the chosen cards to ExileSupport")
        void dispatchDelegates() {
            UUID cardId = UUID.randomUUID();
            registry.begin(gd, new PendingInteraction.MirrorOfFateChoice(PLAYER1_ID, List.of(cardId), 7));
            Player player = new Player(PLAYER1_ID, "Player1");

            boolean handled = registry.dispatchAnswer(gd, player, new InteractionAnswer.CardsChosen(List.of(cardId)));

            assertThat(handled).isTrue();
            verify(exileSupport).handleMirrorOfFateChoice(gd, player, List.of(cardId));
        }

        @Test
        @DisplayName("replayPrompt re-sends only to the decider")
        void replayOnlyToDecider() {
            Card exiled = createCard("Lightning Bolt");
            gd.addToExile(PLAYER1_ID, exiled);
            registry.begin(gd, new PendingInteraction.MirrorOfFateChoice(PLAYER1_ID, List.of(exiled.getId()), 7));
            org.mockito.Mockito.clearInvocations(sessionManager);

            assertThat(registry.replayPrompt(gd, PLAYER2_ID)).isTrue();
            verifyNoInteractions(sessionManager);

            assertThat(registry.replayPrompt(gd, PLAYER1_ID)).isTrue();
            verify(sessionManager).sendToPlayer(eq(PLAYER1_ID), any(ChooseMultipleCardsMessage.class));
        }
    }
}
