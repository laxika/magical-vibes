package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.MayAbilityMessage;
import com.github.laxika.magicalvibes.service.input.MayAbilityHandlerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class MayAbilityChoiceInteractionHandlerTest {

    @Mock private SessionManager sessionManager;
    @Mock private MayAbilityHandlerService mayAbilityHandlerService;

    @InjectMocks
    private MayAbilityChoiceInteractionHandler handler;

    @Captor private ArgumentCaptor<Object> messageCaptor;

    private InteractionHandlerRegistry registry;
    private GameData gd;

    private static final UUID PLAYER1_ID = UUID.randomUUID();
    private static final UUID PLAYER2_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        registry = new InteractionHandlerRegistry();
        registry.register(handler);

        gd = new GameData(UUID.randomUUID(), "test-game", PLAYER1_ID, "Player1");
        gd.playerIds.addAll(List.of(PLAYER1_ID, PLAYER2_ID));
        gd.orderedPlayerIds.addAll(List.of(PLAYER1_ID, PLAYER2_ID));
        gd.playerIdToName.put(PLAYER1_ID, "Player1");
        gd.playerIdToName.put(PLAYER2_ID, "Player2");
        gd.playerManaPools.put(PLAYER1_ID, new ManaPool());
        gd.playerManaPools.put(PLAYER2_ID, new ManaPool());
    }

    private PendingInteraction.MayAbilityChoice choice() {
        return new PendingInteraction.MayAbilityChoice(PLAYER1_ID, "You may draw a card", null);
    }

    @Nested
    @DisplayName("begin")
    class Begin {

        @Test
        @DisplayName("Sets the active interaction and the legacy MAY_ABILITY_CHOICE state")
        void setsInteractionState() {
            registry.begin(gd, choice());

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
            assertThat(gd.interaction.activeInteraction()).isEqualTo(choice());
        }

        @Test
        @DisplayName("Sends MayAbilityMessage with the description and no mana cost")
        void sendsMessage() {
            registry.begin(gd, choice());

            verify(sessionManager).sendToPlayer(eq(PLAYER1_ID), messageCaptor.capture());
            MayAbilityMessage msg = (MayAbilityMessage) messageCaptor.getValue();
            assertThat(msg.prompt()).isEqualTo("You may draw a card");
            assertThat(msg.canPay()).isTrue();
            assertThat(msg.manaCost()).isNull();
        }

        @Test
        @DisplayName("Reports canPay=false when the decider cannot pay the mana cost")
        void reportsCannotPay() {
            registry.begin(gd, new PendingInteraction.MayAbilityChoice(PLAYER1_ID, "You may pay {2}", "{2}"));

            verify(sessionManager).sendToPlayer(eq(PLAYER1_ID), messageCaptor.capture());
            MayAbilityMessage msg = (MayAbilityMessage) messageCaptor.getValue();
            assertThat(msg.canPay()).isFalse();
            assertThat(msg.manaCost()).isEqualTo("{2}");
        }

        @Test
        @DisplayName("Redirects the prompt to the mind controller when the decider is mind-controlled")
        void redirectsToMindController() {
            UUID controllerId = UUID.randomUUID();
            gd.playerIdToName.put(controllerId, "Controller");
            gd.mindControlledPlayerId = PLAYER1_ID;
            gd.mindControllerPlayerId = controllerId;

            registry.begin(gd, choice());

            verify(sessionManager).sendToPlayer(eq(controllerId), messageCaptor.capture());
        }
    }

    @Nested
    @DisplayName("dispatchAnswer")
    class DispatchAnswer {

        @Test
        @DisplayName("Delegates the accept flag to MayAbilityHandlerService")
        void delegatesAccept() {
            registry.begin(gd, choice());
            Player player = new Player(PLAYER1_ID, "Player1");

            boolean handled = registry.dispatchAnswer(gd, player, new InteractionAnswer.MayAbilityChosen(true));

            assertThat(handled).isTrue();
            verify(mayAbilityHandlerService).handleMayAbilityChosen(gd, player, true);
        }

        @Test
        @DisplayName("Delegates a decline to MayAbilityHandlerService")
        void delegatesDecline() {
            registry.begin(gd, choice());
            Player player = new Player(PLAYER1_ID, "Player1");

            boolean handled = registry.dispatchAnswer(gd, player, new InteractionAnswer.MayAbilityChosen(false));

            assertThat(handled).isTrue();
            verify(mayAbilityHandlerService).handleMayAbilityChosen(gd, player, false);
        }

        @Test
        @DisplayName("Returns false when no interaction is active")
        void returnsFalseWhenNothingActive() {
            boolean handled = registry.dispatchAnswer(gd, new Player(PLAYER1_ID, "Player1"),
                    new InteractionAnswer.MayAbilityChosen(true));

            assertThat(handled).isFalse();
            verifyNoInteractions(mayAbilityHandlerService);
        }
    }

    @Nested
    @DisplayName("replayPrompt")
    class ReplayPrompt {

        @Test
        @DisplayName("Re-sends the prompt to the reconnecting decider")
        void resendsToDecider() {
            registry.begin(gd, choice());
            org.mockito.Mockito.clearInvocations(sessionManager);

            boolean handled = registry.replayPrompt(gd, PLAYER1_ID);

            assertThat(handled).isTrue();
            verify(sessionManager).sendToPlayer(eq(PLAYER1_ID), messageCaptor.capture());
            assertThat(messageCaptor.getValue()).isInstanceOf(MayAbilityMessage.class);
        }

        @Test
        @DisplayName("Does not re-send to a reconnecting non-decider but still reports handled")
        void skipsNonDecider() {
            registry.begin(gd, choice());
            org.mockito.Mockito.clearInvocations(sessionManager);

            boolean handled = registry.replayPrompt(gd, PLAYER2_ID);

            assertThat(handled).isTrue();
            verifyNoInteractions(sessionManager);
        }
    }
}
