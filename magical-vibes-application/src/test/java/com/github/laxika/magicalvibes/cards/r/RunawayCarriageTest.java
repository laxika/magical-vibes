package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DevilthornFox;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({RunawayCarriage.class, DevilthornFox.class})
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
        Permanent attacker = addCreatureReady(player1, new DevilthornFox());
        attacker.setAttacking(true);
        addCreatureReady(player2, new RunawayCarriage());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);

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

    @Test
    @DisplayName("End-of-combat sacrifice waits for the delayed trigger to resolve")
    void sacrificeWaitsForDelayedTriggerResolution() {
        addCreatureReady(player1, new RunawayCarriage());

        harness.withAutoStop(TurnStep.END_OF_COMBAT, () -> {
            declareAttackers(List.of(0));
            harness.passUntil(player1, TurnStep.END_OF_COMBAT);
            harness.assertOnBattlefield(player1, "Runaway Carriage");
            resolveAllTriggers();
            harness.assertNotOnBattlefield(player1, "Runaway Carriage");
            harness.assertInGraveyard(player1, "Runaway Carriage");
        });
    }

    @Test
    @DisplayName("Runaway Carriage deals combat damage before its delayed sacrifice")
    void dealsCombatDamageBeforeSacrifice() {
        addCreatureReady(player1, new RunawayCarriage());
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);

        harness.assertLife(player2, 15);
        harness.assertInGraveyard(player1, "Runaway Carriage");
    }

    @Test
    @DisplayName("The delayed trigger cannot sacrifice a Carriage its controller no longer controls")
    void cannotSacrificeAfterControlChanges() {
        Permanent carriage = addCreatureReady(player1, new RunawayCarriage());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
        });
        gd.playerBattlefields.get(player1.getId()).remove(carriage);
        gd.playerBattlefields.get(player2.getId()).add(carriage);
        carriage.setAttacking(false);

        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);

        harness.assertOnBattlefield(player2, "Runaway Carriage");
        harness.assertNotInGraveyard(player1, "Runaway Carriage");
        harness.assertNotInGraveyard(player2, "Runaway Carriage");
    }
}
