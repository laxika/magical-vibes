package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.h.HulkingOgre;
import com.github.laxika.magicalvibes.cards.j.JunkDiver;
import com.github.laxika.magicalvibes.cards.t.ThranDynamo;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Metalworker.class, JunkDiver.class, ThranDynamo.class, HulkingOgre.class})
class MetalworkerTest extends BaseCardTest {

    @Test
    @DisplayName("Produces two colorless mana for each revealed artifact card")
    void producesManaForRevealedArtifacts() {
        addReadyMetalworker();
        JunkDiver junkDiver = new JunkDiver();
        ThranDynamo thranDynamo = new ThranDynamo();
        HulkingOgre nonArtifact = new HulkingOgre();
        harness.setHand(player1, List.of(junkDiver, thranDynamo, nonArtifact));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.RevealAnyNumberOfCardsFromHandChoice choice =
                (PendingInteraction.RevealAnyNumberOfCardsFromHandChoice) gd.interaction.activeInteraction();
        assertThat(choice.validCardIds()).containsExactly(junkDiver.getId(), thranDynamo.getId());

        harness.handleMultipleCardsChosen(player1, List.of(junkDiver.getId(), thranDynamo.getId()));

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(4);
    }

    @Test
    @DisplayName("Allows revealing zero artifact cards")
    void producesNoManaForZeroRevealedArtifacts() {
        addReadyMetalworker();
        HulkingOgre nonArtifact = new HulkingOgre();
        harness.setHand(player1, List.of(nonArtifact));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Produces mana only for the selected artifact cards")
    void producesManaForOnlySelectedArtifacts() {
        addReadyMetalworker();
        JunkDiver selectedArtifact = new JunkDiver();
        ThranDynamo unselectedArtifact = new ThranDynamo();
        HulkingOgre nonArtifact = new HulkingOgre();
        harness.setHand(player1, List.of(selectedArtifact, unselectedArtifact, nonArtifact));

        harness.activateAbility(player1, 0, null, null);

        PendingInteraction.RevealAnyNumberOfCardsFromHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealAnyNumberOfCardsFromHandChoice.class);
        assertThat(choice.validCardIds()).containsExactly(selectedArtifact.getId(), unselectedArtifact.getId());

        harness.handleMultipleCardsChosen(player1, List.of(selectedArtifact.getId()));

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactly(selectedArtifact, unselectedArtifact, nonArtifact);
    }

    @Test
    @DisplayName("Allows choosing zero artifact cards when artifacts are available")
    void producesNoManaWhenNoArtifactsAreSelected() {
        addReadyMetalworker();
        JunkDiver firstArtifact = new JunkDiver();
        ThranDynamo secondArtifact = new ThranDynamo();
        harness.setHand(player1, List.of(firstArtifact, secondArtifact));

        harness.activateAbility(player1, 0, null, null);

        PendingInteraction.RevealAnyNumberOfCardsFromHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealAnyNumberOfCardsFromHandChoice.class);
        assertThat(choice.validCardIds()).containsExactly(firstArtifact.getId(), secondArtifact.getId());

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstArtifact, secondArtifact);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void addReadyMetalworker() {
        addCreatureReady(player1, new Metalworker());
    }

    @Test
    @DisplayName("Mana ability resolves without using the stack and taps Metalworker")
    void resolvesWithoutUsingStack() {
        var metalworker = addCreatureReady(player1, new Metalworker());
        JunkDiver artifact = new JunkDiver();
        harness.setHand(player1, List.of(artifact));

        harness.activateAbility(player1, 0, null, null);

        assertThat(metalworker.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();

        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(artifact);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("tapped");
    }

    @Test
    @DisplayName("Summoning sickness prevents paying the tap cost")
    void cannotActivateWithSummoningSickness() {
        var metalworker = harness.addToBattlefieldAndReturn(player1, new Metalworker());
        metalworker.setSummoningSick(true);
        harness.setHand(player1, List.of(new JunkDiver()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("summoning sickness");

        assertThat(metalworker.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Cannot reveal nonartifacts, opponents' cards, or the same card twice")
    void rejectsInvalidRevealSelections() {
        addReadyMetalworker();
        JunkDiver artifact = new JunkDiver();
        HulkingOgre nonArtifact = new HulkingOgre();
        ThranDynamo opponentsArtifact = new ThranDynamo();
        harness.setHand(player1, List.of(artifact, nonArtifact));
        harness.setHand(player2, List.of(opponentsArtifact));

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(nonArtifact.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(opponentsArtifact.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(artifact.getId(), artifact.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();

        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(opponentsArtifact);
    }
}
