package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

class RunawayCarriageTest extends BaseCardTest {

    @Test
    @DisplayName("Runaway Carriage is sacrificed at end of combat after attacking")
    void sacrificedAtEndOfCombatWhenAttacking() {
        addCreatureReady(player1, new RunawayCarriage());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Runaway Carriage");
        harness.assertInGraveyard(player1, "Runaway Carriage");
    }

    @Test
    @DisplayName("Runaway Carriage is sacrificed at end of combat after blocking")
    void sacrificedAtEndOfCombatWhenBlocking() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        addCreatureReady(player2, new RunawayCarriage());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Runaway Carriage");
        harness.assertInGraveyard(player2, "Runaway Carriage");
    }

    @Test
    @DisplayName("Runaway Carriage is not sacrificed when it neither attacks nor blocks")
    void notSacrificedWhenNotInCombat() {
        addCreatureReady(player1, new RunawayCarriage());

        declareAttackers(List.of());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Runaway Carriage");
    }
}
