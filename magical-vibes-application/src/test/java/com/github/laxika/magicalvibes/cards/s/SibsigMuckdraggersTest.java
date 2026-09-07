package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SibsigMuckdraggersTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a chosen creature card from the graveyard to hand")
    void returnsCreatureToHand() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        castAndResolveEtb();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, 0);

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Only creature cards can be chosen")
    void onlyCreaturesCanBeChosen() {
        Card instant = new Shock();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(instant, creature));
        castAndResolveEtb();

        PendingInteraction.GraveyardChoice choice = gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice.validIndices()).containsExactly(1);
        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid card index");
    }

    @Test
    @DisplayName("Enters the battlefield without a graveyard choice when no creature is available")
    void noCreatureInGraveyard() {
        harness.setGraveyard(player1, List.of(new Shock()));
        castAndResolveEtb();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNull();
        harness.assertOnBattlefield(player1, "Sibsig Muckdraggers");
    }

    @Test
    @DisplayName("Delve exiles graveyard cards to pay the generic cost")
    void delvePaysGenericCost() {
        List<Card> graveyard = List.of(
                new Shock(), new Shock(), new Shock(), new Shock(),
                new Shock(), new Shock(), new Shock(), new Shock());
        harness.setGraveyard(player1, graveyard);
        harness.setHand(player1, List.of(new SibsigMuckdraggers()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(0, 1, 2, 3, 4, 5, 6, 7));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrderElementsOf(graveyard);
        harness.assertOnBattlefield(player1, "Sibsig Muckdraggers");
    }

    private void castAndResolveEtb() {
        harness.setHand(player1, List.of(new SibsigMuckdraggers()));
        harness.addMana(player1, ManaColor.BLACK, 9);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
