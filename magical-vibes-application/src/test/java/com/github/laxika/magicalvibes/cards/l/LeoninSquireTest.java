package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.ConjurersBauble;
import com.github.laxika.magicalvibes.cards.e.EnergyChamber;
import com.github.laxika.magicalvibes.cards.e.EternalWitness;
import com.github.laxika.magicalvibes.cards.p.ParadiseMantle;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LeoninSquire.class, ConjurersBauble.class, ParadiseMantle.class, EnergyChamber.class,
        EternalWitness.class})
class LeoninSquireTest extends BaseCardTest {

    private void castLeoninSquire() {
        harness.castFromHand(player1, new LeoninSquire(), "{1}{W}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB returns a target artifact card with mana value 1 or less from its controller's graveyard")
    void returnsEligibleArtifact() {
        Card oneManaArtifact = new ConjurersBauble();
        Card zeroManaArtifact = new ParadiseMantle();
        harness.setGraveyard(player1, List.of(oneManaArtifact, zeroManaArtifact));

        castLeoninSquire();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(oneManaArtifact.getId(), zeroManaArtifact.getId());

        harness.handleMultipleCardsChosen(player1, List.of(oneManaArtifact.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Conjurer's Bauble");
        harness.assertInGraveyard(player1, "Paradise Mantle");
    }

    @Test
    @DisplayName("ETB cannot target a non-artifact or an artifact with mana value greater than 1")
    void filtersIllegalCards() {
        Card nonArtifact = new EternalWitness();
        Card expensiveArtifact = new EnergyChamber();
        harness.setGraveyard(player1, List.of(nonArtifact, expensiveArtifact));

        castLeoninSquire();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Eternal Witness");
        harness.assertInGraveyard(player1, "Energy Chamber");
    }

    @Test
    @DisplayName("ETB cannot target an artifact card in an opponent's graveyard")
    void onlyTargetsOwnGraveyard() {
        Card artifact = new ConjurersBauble();
        harness.setGraveyard(player2, List.of(artifact));

        castLeoninSquire();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player2, "Conjurer's Bauble");
    }

    @Test
    @DisplayName("ETB returns a zero-mana artifact and leaves the unchosen artifact in the graveyard")
    void returnsZeroManaArtifact() {
        Card zeroManaArtifact = new ParadiseMantle();
        Card oneManaArtifact = new ConjurersBauble();
        harness.setGraveyard(player1, List.of(zeroManaArtifact, oneManaArtifact));

        castLeoninSquire();
        harness.handleMultipleCardsChosen(player1, List.of(zeroManaArtifact.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Paradise Mantle");
        harness.assertNotInGraveyard(player1, "Paradise Mantle");
        harness.assertInGraveyard(player1, "Conjurer's Bauble");
        harness.assertNotInHand(player1, "Conjurer's Bauble");
    }

    @Test
    @DisplayName("ETB does not choose a replacement when its target leaves the graveyard before resolution")
    void removedTargetDoesNotReturnAnotherArtifact() {
        Card target = new ConjurersBauble();
        Card otherArtifact = new ParadiseMantle();
        harness.setGraveyard(player1, List.of(target, otherArtifact));

        castLeoninSquire();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.setGraveyard(player1, List.of(otherArtifact));
        harness.setExile(player1, List.of(target));
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Conjurer's Bauble");
        harness.assertNotInHand(player1, "Paradise Mantle");
        harness.assertInGraveyard(player1, "Paradise Mantle");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
