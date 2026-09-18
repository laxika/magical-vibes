package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.ConjurersBauble;
import com.github.laxika.magicalvibes.cards.e.EnergyChamber;
import com.github.laxika.magicalvibes.cards.e.EternalWitness;
import com.github.laxika.magicalvibes.cards.p.ParadiseMantle;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
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
        harness.setHand(player1, List.of(new LeoninSquire()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
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
}
