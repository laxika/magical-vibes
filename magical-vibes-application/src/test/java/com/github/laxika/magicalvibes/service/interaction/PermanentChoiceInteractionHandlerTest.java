package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.InteractionPromptMessage;
import com.github.laxika.magicalvibes.service.input.PermanentChoiceHandlerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PermanentChoiceInteractionHandlerTest {

    @Mock private PermanentChoiceHandlerService permanentChoiceHandlerService;

    private InteractionHandlerRegistry registry;
    private InteractionProjectionTestSupport projectionSupport;
    private GameData gd;

    private static final UUID PLAYER1_ID = UUID.randomUUID();
    private static final UUID PLAYER2_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        projectionSupport = new InteractionProjectionTestSupport();
        registry = projectionSupport.registry();
        projectionSupport.register(
                new PermanentChoiceInteractionHandler(permanentChoiceHandlerService));

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

        projectionSupport.begin(
                gd,
                choice(
                        List.of(perm1, perm2),
                        List.of(),
                        null,
                        "Choose a creature to sacrifice."));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        InteractionPromptMessage msg = projectionSupport.projectedPrompt(gd);
        assertThat(msg.permanentIds()).containsExactly(perm1, perm2);
        assertThat(msg.playerIds()).isEmpty();
        assertThat(msg.prompt()).isEqualTo("Choose a creature to sacrifice.");
    }

    @Test
    @DisplayName("begin sends the any-target variant with both ordered ID lists")
    void beginSendsAnyTargetVariant() {
        UUID permId = UUID.randomUUID();

        projectionSupport.begin(
                gd,
                choice(
                        List.of(permId), List.of(PLAYER2_ID), null, "Choose any target."));

        InteractionPromptMessage msg = projectionSupport.projectedPrompt(gd);
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
        projectionSupport.begin(
                gd,
                choice(List.of(permId), List.of(), null, "Choose a permanent."));
        Player player = new Player(PLAYER1_ID, "Player1");

        boolean handled = registry.dispatchAnswer(gd, player, new InteractionAnswer.PermanentChosen(permId));

        assertThat(handled).isTrue();
        verify(permanentChoiceHandlerService).handlePermanentChosen(gd, player, permId);
    }

}
