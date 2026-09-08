package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.ChromaticStar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IsochronScepter;
import com.github.laxika.magicalvibes.cards.t.TormodsCrypt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
        Card crypt = new TormodsCrypt();
        Card star = new ChromaticStar();
        harness.setGraveyard(player1, List.of(crypt, star));

        castLeoninSquire();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(crypt.getId(), star.getId());

        harness.handleMultipleCardsChosen(player1, List.of(crypt.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Tormod's Crypt");
        harness.assertInGraveyard(player1, "Chromatic Star");
    }

    @Test
    @DisplayName("ETB cannot target a non-artifact or an artifact with mana value greater than 1")
    void filtersIllegalCards() {
        Card nonArtifact = new GrizzlyBears();
        Card expensiveArtifact = new IsochronScepter();
        harness.setGraveyard(player1, List.of(nonArtifact, expensiveArtifact));

        castLeoninSquire();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Isochron Scepter");
    }

    @Test
    @DisplayName("ETB cannot target an artifact card in an opponent's graveyard")
    void onlyTargetsOwnGraveyard() {
        Card crypt = new TormodsCrypt();
        harness.setGraveyard(player2, List.of(crypt));

        castLeoninSquire();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player2, "Tormod's Crypt");
    }
}
