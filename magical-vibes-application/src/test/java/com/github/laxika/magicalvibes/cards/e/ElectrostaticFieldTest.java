package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JacesIngenuity;
import com.github.laxika.magicalvibes.cards.r.RadicalIdea;
import com.github.laxika.magicalvibes.cards.u.UnexplainedDisappearance;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({ElectrostaticField.class, JacesIngenuity.class, Divination.class, GrizzlyBears.class,
        RadicalIdea.class, UnexplainedDisappearance.class})
class ElectrostaticFieldTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an instant deals 1 damage to each opponent")
    void instantDealsDamageToEachOpponent() {
        harness.addToBattlefield(player1, new ElectrostaticField());
        harness.setHand(player1, List.of(new JacesIngenuity()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player1, 0);

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Casting a sorcery deals 1 damage to each opponent")
    void sorceryDealsDamageToEachOpponent() {
        harness.addToBattlefield(player1, new ElectrostaticField());
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.setLife(player2, 20);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Casting a creature does not trigger Electrostatic Field")
    void creatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new ElectrostaticField());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setLife(player2, 20);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("An opponent casting an instant does not trigger Electrostatic Field")
    void opponentInstantDoesNotTrigger() {
        harness.addToBattlefield(player1, new ElectrostaticField());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new JacesIngenuity()));
        harness.addMana(player2, ManaColor.BLUE, 5);
        harness.setLife(player1, 20);

        harness.castAndResolveInstant(player2, 0);

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Each Field triggers independently without damaging its controller")
    void multipleFieldsTriggerIndependently() {
        harness.addToBattlefield(player1, new ElectrostaticField());
        harness.addToBattlefield(player1, new ElectrostaticField());
        harness.setHand(player1, List.of(new RadicalIdea()));
        harness.setLibrary(player1, List.of(new ElectrostaticField()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0);
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("An opponent-controlled Field damages only that player's opponent")
    void opponentControlledFieldTriggersForItsController() {
        harness.addToBattlefield(player2, new ElectrostaticField());
        harness.setHand(player2, List.of(new RadicalIdea()));
        harness.setLibrary(player2, List.of(new ElectrostaticField()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castInstant(player2, 0);
        resolveAllTriggers();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A pending Field trigger still deals damage after the Field leaves")
    void triggerResolvesAfterSourceLeavesBattlefield() {
        var field = harness.addToBattlefieldAndReturn(player1, new ElectrostaticField());
        harness.setHand(player1, List.of(new RadicalIdea()));
        harness.setHand(player2, List.of(new UnexplainedDisappearance()));
        harness.setLibrary(player1, List.of(new ElectrostaticField()));
        harness.setLibrary(player2, List.of());
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0);
        harness.castInstant(player2, 0, field.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Electrostatic Field");
        harness.assertLife(player2, 20);

        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
    }
}
