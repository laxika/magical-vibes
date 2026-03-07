package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RememberTheFallenTest extends BaseCardTest {

    // ===== Mode 0: Return creature card =====

    @Test
    @DisplayName("Mode 0 — creature in graveyard prompts for creature target")
    void mode0PromptsForCreatureTarget() {
        Card creature = new GrizzlyBears();
        Card artifact = new LeoninScimitar();
        harness.setGraveyard(player1, List.of(creature, artifact));
        harness.setHand(player1, List.of(new RememberTheFallen()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castSorcery(player1, 0, 0); // mode 0 = creature

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MULTI_GRAVEYARD_CHOICE);
        // Only creature should be a valid target
        assertThat(gd.interaction.multiSelection().multiGraveyardValidCardIds()).hasSize(1);
        assertThat(gd.interaction.multiSelection().multiGraveyardValidCardIds()).contains(creature.getId());
        assertThat(gd.interaction.multiSelection().multiGraveyardMaxCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Mode 0 — selecting creature returns it to hand")
    void mode0ReturnsCreatureToHand() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new RememberTheFallen()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castSorcery(player1, 0, 0);
        List<UUID> validIds = new ArrayList<>(gd.interaction.multiSelection().multiGraveyardValidCardIds());
        harness.handleMultipleGraveyardCardsChosen(player1, validIds);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Mode 0 — no creatures in graveyard skips prompt")
    void mode0NoCreaturesSkipsPrompt() {
        harness.setGraveyard(player1, List.of(new LeoninScimitar()));
        harness.setHand(player1, List.of(new RememberTheFallen()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.interaction.awaitingInputType()).isNull();
        assertThat(gd.stack).hasSize(1);
    }

    // ===== Mode 1: Return artifact card =====

    @Test
    @DisplayName("Mode 1 — artifact in graveyard prompts for artifact target")
    void mode1PromptsForArtifactTarget() {
        Card creature = new GrizzlyBears();
        Card artifact = new LeoninScimitar();
        harness.setGraveyard(player1, List.of(creature, artifact));
        harness.setHand(player1, List.of(new RememberTheFallen()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castSorcery(player1, 0, 1); // mode 1 = artifact

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MULTI_GRAVEYARD_CHOICE);
        // Only artifact should be a valid target
        assertThat(gd.interaction.multiSelection().multiGraveyardValidCardIds()).hasSize(1);
        assertThat(gd.interaction.multiSelection().multiGraveyardValidCardIds()).contains(artifact.getId());
        assertThat(gd.interaction.multiSelection().multiGraveyardMaxCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Mode 1 — selecting artifact returns it to hand")
    void mode1ReturnsArtifactToHand() {
        Card artifact = new LeoninScimitar();
        harness.setGraveyard(player1, List.of(artifact));
        harness.setHand(player1, List.of(new RememberTheFallen()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castSorcery(player1, 0, 1);
        List<UUID> validIds = new ArrayList<>(gd.interaction.multiSelection().multiGraveyardValidCardIds());
        harness.handleMultipleGraveyardCardsChosen(player1, validIds);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Leonin Scimitar"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Leonin Scimitar"));
    }

    @Test
    @DisplayName("Mode 1 — no artifacts in graveyard skips prompt")
    void mode1NoArtifactsSkipsPrompt() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new RememberTheFallen()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castSorcery(player1, 0, 1);

        assertThat(gd.interaction.awaitingInputType()).isNull();
        assertThat(gd.stack).hasSize(1);
    }

    // ===== Mode 2: Return both creature and artifact =====

    @Test
    @DisplayName("Mode 2 — both creature and artifact in graveyard prompts with both valid")
    void mode2PromptsForBothTargets() {
        Card creature = new GrizzlyBears();
        Card artifact = new LeoninScimitar();
        harness.setGraveyard(player1, List.of(creature, artifact));
        harness.setHand(player1, List.of(new RememberTheFallen()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castSorcery(player1, 0, 2); // mode 2 = both

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MULTI_GRAVEYARD_CHOICE);
        assertThat(gd.interaction.multiSelection().multiGraveyardValidCardIds()).hasSize(2);
        assertThat(gd.interaction.multiSelection().multiGraveyardValidCardIds())
                .contains(creature.getId(), artifact.getId());
        assertThat(gd.interaction.multiSelection().multiGraveyardMaxCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("Mode 2 — selecting both creature and artifact returns them to hand")
    void mode2ReturnsBothToHand() {
        Card creature = new GrizzlyBears();
        Card artifact = new LeoninScimitar();
        harness.setGraveyard(player1, List.of(creature, artifact));
        harness.setHand(player1, List.of(new RememberTheFallen()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castSorcery(player1, 0, 2);
        List<UUID> validIds = new ArrayList<>(gd.interaction.multiSelection().multiGraveyardValidCardIds());
        harness.handleMultipleGraveyardCardsChosen(player1, validIds);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"))
                .anyMatch(c -> c.getName().equals("Leonin Scimitar"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"))
                .noneMatch(c -> c.getName().equals("Leonin Scimitar"));
    }

    @Test
    @DisplayName("Mode 2 — non-creature/non-artifact cards are excluded from targets")
    void mode2ExcludesNonCreatureNonArtifactCards() {
        Card creature = new GrizzlyBears();
        Card sorcery = new RememberTheFallen();
        harness.setGraveyard(player1, List.of(creature, sorcery));
        harness.setHand(player1, List.of(new RememberTheFallen()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castSorcery(player1, 0, 2);

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MULTI_GRAVEYARD_CHOICE);
        // Only creature qualifies — sorcery is neither creature nor artifact
        assertThat(gd.interaction.multiSelection().multiGraveyardValidCardIds()).hasSize(1);
        assertThat(gd.interaction.multiSelection().multiGraveyardValidCardIds()).contains(creature.getId());
    }

    // ===== Spell goes to graveyard after resolution =====

    @Test
    @DisplayName("Remember the Fallen goes to graveyard after resolution")
    void spellGoesToGraveyardAfterResolution() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new RememberTheFallen()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castSorcery(player1, 0, 0);
        List<UUID> validIds = new ArrayList<>(gd.interaction.multiSelection().multiGraveyardValidCardIds());
        harness.handleMultipleGraveyardCardsChosen(player1, validIds);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Remember the Fallen"));
    }
}
