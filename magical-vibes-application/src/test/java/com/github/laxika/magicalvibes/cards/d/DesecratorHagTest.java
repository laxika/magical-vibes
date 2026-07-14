package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DesecratorHagTest extends BaseCardTest {

    /**
     * Casts Desecrator Hag and resolves it plus its (non-may) ETB trigger. Any resulting
     * graveyard choice is left active for the caller to answer.
     */
    private void castAndResolveEtb() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new DesecratorHag()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell → ETB triggers
        harness.passBothPriorities(); // resolve ETB → graveyard choice (if any)
    }

    @Test
    @DisplayName("Returns the greatest-power creature from the graveyard")
    void returnsGreatestPowerCreature() {
        // index 0 = Grizzly Bears (2 power), index 1 = Hill Giant (3 power)
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new HillGiant()));
        castAndResolveEtb();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);

        harness.handleGraveyardCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Hill Giant"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"))
                .noneMatch(c -> c.getName().equals("Hill Giant"));
    }

    @Test
    @DisplayName("A lower-power creature cannot be chosen")
    void lowerPowerCreatureNotSelectable() {
        // index 0 = Grizzly Bears (2 power) is not the greatest; only Hill Giant (index 1) is.
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new HillGiant()));
        castAndResolveEtb();

        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid card index");
    }

    @Test
    @DisplayName("Ties for greatest power let the controller choose one")
    void tieLetsControllerChoose() {
        // Air Elemental and Serra Angel are both 4 power; Grizzly Bears (2) is not selectable.
        harness.setGraveyard(player1, List.of(new AirElemental(), new SerraAngel(), new GrizzlyBears()));
        castAndResolveEtb();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);

        // Choose Serra Angel (index 1) among the tied pair.
        harness.handleGraveyardCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Serra Angel"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Air Elemental"))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("A non-greatest creature cannot be chosen when powers are tied")
    void nonTiedCreatureNotSelectable() {
        harness.setGraveyard(player1, List.of(new AirElemental(), new SerraAngel(), new GrizzlyBears()));
        castAndResolveEtb();

        // index 2 = Grizzly Bears (2 power) is below the tied greatest of 4.
        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player1, 2))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid card index");
    }

    @Test
    @DisplayName("The mandatory return cannot be declined")
    void cannotDeclineMandatoryReturn() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new HillGiant()));
        castAndResolveEtb();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);

        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player1, -1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot decline forced graveyard choice");
    }

    @Test
    @DisplayName("No graveyard choice when there are no creature cards in the graveyard")
    void noChoiceWithoutCreatures() {
        harness.setGraveyard(player1, List.of(new HolyDay()));
        castAndResolveEtb();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Holy Day"));
    }
}
