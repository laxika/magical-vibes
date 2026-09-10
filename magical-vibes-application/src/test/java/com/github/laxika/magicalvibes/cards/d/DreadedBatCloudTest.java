package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({DreadedBatCloud.class, GrizzlyBears.class, Shock.class})
class DreadedBatCloudTest extends BaseCardTest {

    @Test
    @DisplayName("Costs {3} less when a creature died this turn")
    void costsLessWithMorbid() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new DreadedBatCloud()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        gd.creatureDeathCountThisTurn.merge(player2.getId(), 1, Integer::sum);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Dreaded Bat-Cloud");
    }

    @Test
    @DisplayName("Cannot use the reduced cost when no creature died this turn")
    void requiresFullCostWithoutMorbid() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new DreadedBatCloud()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        org.junit.jupiter.api.Assertions.assertThrows(Exception.class, () ->
                harness.castCreature(player1, 0));
    }

    @Test
    @DisplayName("A creature killed by Shock enables the reduced cost")
    void actualCreatureDeathEnablesReduction() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Shock(), new DreadedBatCloud()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addToBattlefield(player2, new GrizzlyBears());

        java.util.UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, bearsId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Dreaded Bat-Cloud");
    }
}
