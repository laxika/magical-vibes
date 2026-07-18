package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.InteractionPromptMessage;
import com.github.laxika.magicalvibes.service.input.PermanentChoiceHandlerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class PermanentChoiceInteractionHandlerTest {

    @Mock private SessionManager sessionManager;
    @Mock private PermanentChoiceHandlerService permanentChoiceHandlerService;

    @Captor private ArgumentCaptor<Object> messageCaptor;

    private InteractionHandlerRegistry registry;
    private GameData gd;

    private static final UUID PLAYER1_ID = UUID.randomUUID();
    private static final UUID PLAYER2_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        registry = new InteractionHandlerRegistry();
        registry.register(new PermanentChoiceInteractionHandler(sessionManager, permanentChoiceHandlerService));

        gd = new GameData(UUID.randomUUID(), "test-game", PLAYER1_ID, "Player1");
        gd.playerIds.addAll(List.of(PLAYER1_ID, PLAYER2_ID));
        gd.orderedPlayerIds.addAll(List.of(PLAYER1_ID, PLAYER2_ID));
        gd.playerIdToName.put(PLAYER1_ID, "Player1");
        gd.playerIdToName.put(PLAYER2_ID, "Player2");
    }

    private PendingInteraction.PermanentChoice choice(List<UUID> permanentIds, List<UUID> playerIds,
                                                      PermanentChoiceContext context, String prompt) {
        return new PendingInteraction.PermanentChoice(PLAYER1_ID, permanentIds, playerIds, context, prompt);
    }

    @Test
    @DisplayName("begin sets PERMANENT_CHOICE and sends the plain-variant message with an empty player list")
    void beginSendsPlainVariant() {
        UUID perm1 = UUID.randomUUID();
        UUID perm2 = UUID.randomUUID();

        registry.begin(gd, choice(List.of(perm1, perm2), List.of(), null, "Choose a creature to sacrifice."));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        verify(sessionManager).sendToPlayer(eq(PLAYER1_ID), messageCaptor.capture());
        InteractionPromptMessage msg = (InteractionPromptMessage) messageCaptor.getValue();
        assertThat(msg.permanentIds()).containsExactly(perm1, perm2);
        assertThat(msg.playerIds()).isEmpty();
        assertThat(msg.prompt()).isEqualTo("Choose a creature to sacrifice.");
    }

    @Test
    @DisplayName("begin sends the any-target variant with both ordered ID lists")
    void beginSendsAnyTargetVariant() {
        UUID permId = UUID.randomUUID();

        registry.begin(gd, choice(List.of(permId), List.of(PLAYER2_ID), null, "Choose any target."));

        verify(sessionManager).sendToPlayer(eq(PLAYER1_ID), messageCaptor.capture());
        InteractionPromptMessage msg = (InteractionPromptMessage) messageCaptor.getValue();
        assertThat(msg.permanentIds()).containsExactly(permId);
        assertThat(msg.playerIds()).containsExactly(PLAYER2_ID);
        assertThat(msg.prompt()).isEqualTo("Choose any target.");
    }

    @Test
    @DisplayName("validIds merges the permanent and player lists")
    void validIdsMergesBothLists() {
        UUID permId = UUID.randomUUID();
        PendingInteraction.PermanentChoice interaction = choice(List.of(permId), List.of(PLAYER2_ID), null, "p");

        assertThat(interaction.validIds()).containsExactlyInAnyOrder(permId, PLAYER2_ID);
    }

    @Test
    @DisplayName("dispatchAnswer delegates the chosen ID to PermanentChoiceHandlerService")
    void dispatchDelegates() {
        UUID permId = UUID.randomUUID();
        registry.begin(gd, choice(List.of(permId), List.of(), null, "Choose a permanent."));
        Player player = new Player(PLAYER1_ID, "Player1");

        boolean handled = registry.dispatchAnswer(gd, player, new InteractionAnswer.PermanentChosen(permId));

        assertThat(handled).isTrue();
        verify(permanentChoiceHandlerService).handlePermanentChosen(gd, player, permId);
    }

    @Test
    @DisplayName("replayPrompt re-sends only to the decider")
    void replayOnlyToDecider() {
        UUID permId = UUID.randomUUID();
        registry.begin(gd, choice(List.of(permId), List.of(PLAYER2_ID), null, "Choose any target."));
        org.mockito.Mockito.clearInvocations(sessionManager);

        assertThat(registry.replayPrompt(gd, PLAYER2_ID)).isTrue();
        verifyNoInteractions(sessionManager);

        assertThat(registry.replayPrompt(gd, PLAYER1_ID)).isTrue();
        verify(sessionManager).sendToPlayer(eq(PLAYER1_ID), any(InteractionPromptMessage.class));
    }
}
