package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.Bonesplitter;
import com.github.laxika.magicalvibes.cards.f.Frogmite;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.t.TelJiladExile;
import com.github.laxika.magicalvibes.model.ManaColor;
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
        harness.setHand(player1, List.of(new MoriokScavenger()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
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
    @DisplayName("The optional return can be declined")
    void returnCanBeDeclined() {
        Ornithopter ornithopter = new Ornithopter();
        harness.setGraveyard(player1, List.of(ornithopter));

        castMoriokScavenger();

        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Ornithopter");
        harness.assertNotInHand(player1, "Ornithopter");
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
