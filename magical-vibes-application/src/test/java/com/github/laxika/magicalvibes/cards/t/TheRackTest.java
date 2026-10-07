package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IvoryMask;
import com.github.laxika.magicalvibes.cards.s.StealArtifact;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheRack.class, GrizzlyBears.class, StealArtifact.class, IvoryMask.class})
class TheRackTest extends BaseCardTest {

    @Test
    @DisplayName("Chosen opponent's upkeep with empty hand deals 3 damage")
    void emptyHandDealsThree() {
        harness.addToBattlefield(player1, new TheRack());
        harness.setHand(player2, List.of());
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 3);
    }

    @Test
    @DisplayName("Chosen opponent's upkeep with one card deals 2 damage")
    void oneCardDealsTwo() {
        harness.addToBattlefield(player1, new TheRack());
        harness.setHand(player2, List.of(new GrizzlyBears()));
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("Chosen opponent's upkeep with three cards deals no damage")
    void threeCardsDealsNothing() {
        harness.addToBattlefield(player1, new TheRack());
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("A full hand floors the damage at zero (never gains life)")
    void fullHandFlooredAtZero() {
        harness.addToBattlefield(player1, new TheRack());
        harness.setHand(player2, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears()));
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Does not trigger on the controller's own upkeep")
    void doesNotTriggerOnControllerUpkeep() {
        harness.addToBattlefield(player1, new TheRack());
        harness.setHand(player1, List.of());
        int controllerLifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(controllerLifeBefore);
    }

    @Test
    void usesHandSizeAtResolution() {
        harness.addToBattlefield(player1, new TheRack());
        harness.setHand(player2, List.of(new GrizzlyBears()));
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.setHand(player2, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 3);
    }

    @Test
    @DisplayName("Still damages the chosen opponent after control changes")
    void chosenOpponentRemainsAffectedAfterControlChange() {
        var rack = harness.addToBattlefieldAndReturn(player1, new TheRack());
        harness.setHand(player2, List.of(new StealArtifact()));
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castEnchantment(player2, 0, rack.getId());
        harness.passBothPriorities();

        int player1LifeBefore = gd.playerLifeTotals.get(player1.getId());
        int player2LifeBefore = gd.playerLifeTotals.get(player2.getId());
        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(player1LifeBefore);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(player2LifeBefore - 3);
    }
    @Test
    @DisplayName("Choosing an opponent as The Rack enters does not create a triggered ability")
    void choosesOpponentAsItEntersWithoutUsingTheStack() {
        harness.castFromHand(player1, new TheRack(), "{1}");
        harness.passBothPriorities();
        if (gd.interaction.isAwaitingInput()) {
            harness.handlePermanentChosen(player1, player2.getId());
        }

        harness.assertOnBattlefield(player1, "The Rack");
        assertThat(gd.stack).isEmpty();
        harness.setHand(player2, List.of());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("The upkeep ability damages the chosen player even if that player has shroud")
    void upkeepDamageDoesNotTargetTheChosenPlayer() {
        harness.addToBattlefield(player1, new TheRack());
        harness.addToBattlefield(player2, new IvoryMask());
        harness.setHand(player2, List.of());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Chosen opponent's upkeep with two cards deals 1 damage")
    void twoCardsDealsOne() {
        harness.addToBattlefield(player1, new TheRack());
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }
}
