package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.j.JoustingLance;
import com.github.laxika.magicalvibes.cards.p.PrimordialWurm;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ExcavationElephant.class, JoustingLance.class, PrimordialWurm.class})
class ExcavationElephantTest extends BaseCardTest {

    @Test
    @DisplayName("Cast without kicker enters without an ETB trigger")
    void castWithoutKickerNoTrigger() {
        harness.castFromHand(player1, new ExcavationElephant(), "{4}{W}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Excavation Elephant");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cast without kicker ignores an artifact in the graveyard")
    void castWithoutKickerIgnoresGraveyard() {
        harness.setGraveyard(player1, List.of(new JoustingLance()));
        harness.castFromHand(player1, new ExcavationElephant(), "{4}{W}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Jousting Lance");
    }

    @Test
    @DisplayName("Kicked ETB chooses its artifact target before players can respond")
    void castWithKickerPutsEtbOnStack() {
        JoustingLance artifact = new JoustingLance();
        harness.setGraveyard(player1, List.of(artifact));
        castKicked();

        harness.assertOnBattlefield(player1, "Excavation Elephant");
        chooseArtifactTarget(artifact);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getTargetCardIds()).containsExactly(artifact.getId());
        harness.assertInGraveyard(player1, "Jousting Lance");
    }

    @Test
    @DisplayName("Kicked ETB returns the targeted artifact to hand without another choice")
    void castWithKickerReturnsArtifact() {
        JoustingLance artifact = new JoustingLance();
        harness.setGraveyard(player1, List.of(artifact));
        castKicked();
        chooseArtifactTarget(artifact);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInHand(player1, "Jousting Lance");
        harness.assertNotInGraveyard(player1, "Jousting Lance");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Kicked ETB has no legal target when only non-artifact cards are in the graveyard")
    void castWithKickerDoesNotOfferNonArtifact() {
        harness.setGraveyard(player1, List.of(new PrimordialWurm()));
        castKicked();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Primordial Wurm");
    }

    @Test
    @DisplayName("Kicked ETB is removed from the stack when there are no legal targets")
    void castWithKickerEmptyGraveyard() {
        castKicked();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Kicked ETB requires one artifact target and only offers cards from your graveyard")
    void targetingIsMandatoryAndRestrictedToOwnArtifacts() {
        JoustingLance artifact = new JoustingLance();
        PrimordialWurm creature = new PrimordialWurm();
        JoustingLance opposingArtifact = new JoustingLance();
        harness.setGraveyard(player1, List.of(creature, artifact));
        harness.setGraveyard(player2, List.of(opposingArtifact));
        castKicked();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.minCount()).isEqualTo(1);
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.cards()).containsExactly(artifact);
        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Jousting Lance");
        harness.assertInGraveyard(player1, "Primordial Wurm");
        harness.assertInGraveyard(player2, "Jousting Lance");
    }

    @Test
    @DisplayName("An invalidated artifact target cannot be replaced by another artifact at resolution")
    void removedTargetDoesNotReturnAnotherArtifact() {
        JoustingLance target = new JoustingLance();
        JoustingLance otherArtifact = new JoustingLance();
        harness.setGraveyard(player1, List.of(target, otherArtifact));
        castKicked();
        chooseArtifactTarget(target);

        harness.setGraveyard(player1, List.of(otherArtifact));
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertNotInHand(player1, "Jousting Lance");
        harness.assertInGraveyard(player1, "Jousting Lance");
    }

    private void chooseArtifactTarget(JoustingLance artifact) {
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));
    }

    private void castKicked() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ExcavationElephant()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
    }
}
