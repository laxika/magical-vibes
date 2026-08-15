package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FortuitousFindTest extends BaseCardTest {

    @Test
    void artifactModeReturnsArtifactCardToHand() {
        Card artifact = new FountainOfYouth();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(artifact, creature));
        harness.setHand(player1, List.of(new FortuitousFind()));
        addMana();

        harness.castSorcery(player1, 0, 0);

        assertThat(graveyardChoice().validCardIds()).containsExactly(artifact.getId());
        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Fountain of Youth");
        harness.assertNotInGraveyard(player1, "Fountain of Youth");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void creatureModeReturnsCreatureCardToHand() {
        Card artifact = new FountainOfYouth();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(artifact, creature));
        harness.setHand(player1, List.of(new FortuitousFind()));
        addMana();

        harness.castSorcery(player1, 0, 1);

        assertThat(graveyardChoice().validCardIds()).containsExactly(creature.getId());
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Fountain of Youth");
    }

    @Test
    void bothModesReturnArtifactAndCreatureCardsToHand() {
        Card artifact = new FountainOfYouth();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(artifact, creature));
        harness.setHand(player1, List.of(new FortuitousFind()));
        addMana();

        harness.castSorcery(player1, 0, 2);

        assertThat(graveyardChoice().validCardIds()).containsExactly(artifact.getId(), creature.getId());
        assertThat(graveyardChoice().maxCount()).isEqualTo(2);
        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId(), creature.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Fountain of Youth");
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Fountain of Youth");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void bothModeExcludesCardsThatAreNeitherArtifactsNorCreatures() {
        Card creature = new GrizzlyBears();
        Card instant = new FortuitousFind();
        harness.setGraveyard(player1, List.of(creature, instant));
        harness.setHand(player1, List.of(new FortuitousFind()));
        addMana();

        harness.castSorcery(player1, 0, 2);

        assertThat(graveyardChoice().validCardIds()).containsExactly(creature.getId());
    }

    private PendingInteraction.MultiGraveyardChoice graveyardChoice() {
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        return gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
