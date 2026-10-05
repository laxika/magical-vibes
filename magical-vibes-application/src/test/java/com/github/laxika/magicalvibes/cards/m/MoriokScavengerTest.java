package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.Bonesplitter;
import com.github.laxika.magicalvibes.cards.f.Frogmite;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.t.TelJiladExile;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MoriokScavenger.class, Ornithopter.class, Frogmite.class, Bonesplitter.class,
        TelJiladExile.class})
class MoriokScavengerTest extends BaseCardTest {

    private void castMoriokScavenger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new MoriokScavenger(), "{3}{B}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB returns the chosen artifact creature card to hand")
    void returnsArtifactCreatureToHand() {
        Ornithopter ornithopter = new Ornithopter();
        harness.setGraveyard(player1, List.of(ornithopter));

        castMoriokScavenger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(ornithopter.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Ornithopter");
        harness.assertNotInGraveyard(player1, "Ornithopter");
    }

    @Test
    @DisplayName("Only the selected card returns when multiple artifact creatures are legal")
    void returnsOnlySelectedArtifactCreature() {
        Ornithopter ornithopter = new Ornithopter();
        Frogmite frogmite = new Frogmite();
        harness.setGraveyard(player1, List.of(ornithopter, frogmite));

        castMoriokScavenger();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)
                .validCardIds()).containsExactlyInAnyOrder(ornithopter.getId(), frogmite.getId());
        harness.handleMultipleCardsChosen(player1, List.of(frogmite.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Frogmite");
        harness.assertInGraveyard(player1, "Ornithopter");
    }

    @Test
    @DisplayName("Only artifact creature cards are legal targets")
    void onlyArtifactCreaturesAreLegalTargets() {
        Ornithopter ornithopter = new Ornithopter();
        harness.setGraveyard(player1, List.of(new Bonesplitter(), new TelJiladExile(), ornithopter));

        castMoriokScavenger();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)
                .validCardIds()).containsExactly(ornithopter.getId());
    }

    @Test
    @DisplayName("The optional return can be declined when the targeted trigger resolves")
    void returnCanBeDeclined() {
        Ornithopter ornithopter = new Ornithopter();
        harness.setGraveyard(player1, List.of(ornithopter));

        castMoriokScavenger();

        harness.handleMultipleCardsChosen(player1, List.of(ornithopter.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Ornithopter");
        harness.assertNotInHand(player1, "Ornithopter");
    }

    @Test
    @DisplayName("A legal target must be chosen even when the controller intends to decline the return")
    void targetSelectionIsMandatory() {
        Ornithopter ornithopter = new Ornithopter();
        harness.setGraveyard(player1, List.of(ornithopter));

        castMoriokScavenger();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)
                .minCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Artifact creatures in the opponent's graveyard cannot be targeted")
    void cannotTargetOpponentsGraveyard() {
        Ornithopter ornithopter = new Ornithopter();
        Frogmite frogmite = new Frogmite();
        harness.setGraveyard(player1, List.of(ornithopter));
        harness.setGraveyard(player2, List.of(frogmite));

        castMoriokScavenger();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)
                .validCardIds()).containsExactly(ornithopter.getId());
    }

    @Test
    @DisplayName("The return can be accepted when the targeted trigger resolves")
    void returnCanBeAcceptedAtResolution() {
        Ornithopter ornithopter = new Ornithopter();
        harness.setGraveyard(player1, List.of(ornithopter));

        castMoriokScavenger();
        harness.handleMultipleCardsChosen(player1, List.of(ornithopter.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Ornithopter");
        harness.assertNotInGraveyard(player1, "Ornithopter");
    }

    @Test
    @DisplayName("A target that leaves the graveyard is not replaced by another legal card")
    void missingTargetDoesNotReturnAnotherCard() {
        Ornithopter ornithopter = new Ornithopter();
        Frogmite frogmite = new Frogmite();
        harness.setGraveyard(player1, List.of(ornithopter, frogmite));

        castMoriokScavenger();
        harness.handleMultipleCardsChosen(player1, List.of(ornithopter.getId()));
        harness.setGraveyard(player1, List.of(frogmite));
        harness.setExile(player1, List.of(ornithopter));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotInHand(player1, "Ornithopter");
        harness.assertNotInHand(player1, "Frogmite");
        harness.assertInGraveyard(player1, "Frogmite");
    }

    @Test
    @DisplayName("No artifact creature cards in graveyard produces no prompt")
    void noArtifactCreaturesProducesNoPrompt() {
        harness.setGraveyard(player1, List.of(new Bonesplitter(), new TelJiladExile()));

        castMoriokScavenger();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Bonesplitter");
        harness.assertInGraveyard(player1, "Tel-Jilad Exile");
    }
}
