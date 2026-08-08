package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

class StrawGolemTest extends BaseCardTest {

    @Test
    @DisplayName("An opponent casting a creature spell sacrifices Straw Golem")
    void opponentCastingCreatureSpellSacrificesGolem() {
        harness.addToBattlefield(player1, new StrawGolem());

        opponentCastsCreatureSpell();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Straw Golem");
        harness.assertInGraveyard(player1, "Straw Golem");
    }

    @Test
    @DisplayName("An opponent casting a noncreature spell does not sacrifice Straw Golem")
    void opponentCastingNoncreatureSpellDoesNotSacrificeGolem() {
        harness.addToBattlefield(player1, new StrawGolem());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Opt()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castInstant(player2, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Straw Golem");
    }

    @Test
    @DisplayName("Controller casting a creature spell does not sacrifice Straw Golem")
    void controllerCastingCreatureSpellDoesNotSacrificeGolem() {
        harness.addToBattlefield(player1, new StrawGolem());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Straw Golem");
    }

    private void opponentCastsCreatureSpell() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castCreature(player2, 0);
    }
}
