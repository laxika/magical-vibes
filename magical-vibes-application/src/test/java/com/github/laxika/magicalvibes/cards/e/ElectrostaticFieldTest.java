package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JacesIngenuity;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

class ElectrostaticFieldTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an instant deals 1 damage to each opponent")
    void instantDealsDamageToEachOpponent() {
        harness.addToBattlefield(player1, new ElectrostaticField());
        harness.setHand(player1, List.of(new JacesIngenuity()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Casting a sorcery deals 1 damage to each opponent")
    void sorceryDealsDamageToEachOpponent() {
        harness.addToBattlefield(player1, new ElectrostaticField());
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

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

        harness.castInstant(player2, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }
}
