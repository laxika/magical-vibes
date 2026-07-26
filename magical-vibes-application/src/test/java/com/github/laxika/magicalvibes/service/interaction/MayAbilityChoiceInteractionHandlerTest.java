package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.InteractionPromptMessage;
import com.github.laxika.magicalvibes.service.input.MayAbilityHandlerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class MayAbilityChoiceInteractionHandlerTest {

    @Mock private MayAbilityHandlerService mayAbilityHandlerService;

    @InjectMocks
    private MayAbilityChoiceInteractionHandler handler;

    private InteractionHandlerRegistry registry;
    private InteractionProjectionTestSupport projectionSupport;
    private GameData gd;

    private static final UUID PLAYER1_ID = UUID.randomUUID();
    private static final UUID PLAYER2_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        projectionSupport = new InteractionProjectionTestSupport();
        registry = projectionSupport.registry();
        projectionSupport.register(handler);

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
            projectionSupport.begin(gd, choice());

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
            assertThat(gd.interaction.activeInteraction()).isEqualTo(choice());
        }

        @Test
        @DisplayName("Sends InteractionPromptMessage with the description and no mana cost")
        void sendsMessage() {
            InteractionPromptMessage msg = projectionSupport.begin(gd, choice());
            assertThat(msg.prompt()).isEqualTo("You may draw a card");
            assertThat(msg.canPay()).isTrue();
            assertThat(msg.manaCost()).isNull();
        }

        @Test
        @DisplayName("Reports canPay=false when the decider cannot pay the mana cost")
        void reportsCannotPay() {
            InteractionPromptMessage msg = projectionSupport.begin(
                    gd,
                    new PendingInteraction.MayAbilityChoice(
                            PLAYER1_ID, "You may pay {2}", "{2}"));
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

            projectionSupport.begin(gd, choice());
        }
    }

    @Nested
    @DisplayName("dispatchAnswer")
    class DispatchAnswer {

        @Test
        @DisplayName("Delegates the accept flag to MayAbilityHandlerService")
        void delegatesAccept() {
            projectionSupport.begin(gd, choice());
            Player player = new Player(PLAYER1_ID, "Player1");

            boolean handled = registry.dispatchAnswer(gd, player, new InteractionAnswer.MayAbilityChosen(true));

            assertThat(handled).isTrue();
            verify(mayAbilityHandlerService).handleMayAbilityChosen(gd, player, true);
        }

        @Test
        @DisplayName("Delegates a decline to MayAbilityHandlerService")
        void delegatesDecline() {
            projectionSupport.begin(gd, choice());
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
            projectionSupport.begin(gd, choice());

            boolean handled = registry.replayPrompt(gd, PLAYER1_ID);

            assertThat(handled).isTrue();
            assertThat(projectionSupport.projectedPrompt(gd))
                    .isInstanceOf(InteractionPromptMessage.class);
        }

        @Test
        @DisplayName("Does not re-send to a reconnecting non-decider but still reports handled")
        void skipsNonDecider() {
            projectionSupport.begin(gd, choice());

            boolean handled = registry.replayPrompt(gd, PLAYER2_ID);

            assertThat(handled).isTrue();
        }
    }
}
