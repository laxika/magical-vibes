package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BenalishInfantry;
import com.github.laxika.magicalvibes.cards.f.ForiysianBrigade;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({HeatStroke.class, ForiysianBrigade.class, BenalishInfantry.class})
class HeatStrokeTest extends BaseCardTest {

    @Test
    @DisplayName("At end of combat both the blocked attacker and its blocker are destroyed")
    void destroysBlockerAndBlockedAttacker() {
        Permanent attacker = addCreatureReady(player1, new ForiysianBrigade());
        attacker.setAttacking(true);
        addCreatureReady(player2, new ForiysianBrigade());
        harness.addToBattlefield(player1, new HeatStroke());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        // Combat damage resolves, then the end-of-combat trigger resolves.
        resolveCombat();
        harness.assertOnBattlefield(player1, "Foriysian Brigade");
        harness.assertOnBattlefield(player2, "Foriysian Brigade");
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Foriysian Brigade");
        harness.assertInGraveyard(player1, "Foriysian Brigade");
        harness.assertNotOnBattlefield(player2, "Foriysian Brigade");
        harness.assertInGraveyard(player2, "Foriysian Brigade");
        harness.assertOnBattlefield(player1, "Heat Stroke");
    }

    @Test
    @DisplayName("A creature that stayed out of the block is untouched")
    void sparesCreaturesThatDidNotBlock() {
        Permanent attacker = addCreatureReady(player1, new ForiysianBrigade());
        attacker.setAttacking(true);
        addCreatureReady(player2, new ForiysianBrigade());
        addCreatureReady(player2, new BenalishInfantry());
        harness.addToBattlefield(player1, new HeatStroke());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        resolveCombat();
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Benalish Infantry");
    }

    @Test
    @DisplayName("With no blocks declared, the unblocked attacker survives end of combat")
    void sparesUnblockedAttacker() {
        Permanent attacker = addCreatureReady(player1, new ForiysianBrigade());
        attacker.setAttacking(true);
        harness.addToBattlefield(player1, new HeatStroke());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        resolveCombat();
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Foriysian Brigade");
    }
}
