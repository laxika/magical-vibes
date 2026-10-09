package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CrumblingColossus.class, RuneclawBear.class})
class CrumblingColossusTest extends BaseCardTest {

    @Test
    @DisplayName("Crumbling Colossus deals its combat damage, then is sacrificed at end of combat")
    void sacrificedAtEndOfCombatAfterAttacking() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new CrumblingColossus());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        harness.assertLife(player2, 13);
        resolveAllTriggers();
        harness.assertNotOnBattlefield(player1, "Crumbling Colossus");
        harness.assertInGraveyard(player1, "Crumbling Colossus");
    }

    @Test
    @DisplayName("Blocking with Crumbling Colossus does not trigger the sacrifice")
    void blockingDoesNotTriggerSacrifice() {
        addCreatureReady(player2, new CrumblingColossus());
        Permanent attacker = addCreatureReady(player1, new RuneclawBear());
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).isEmpty();

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Crumbling Colossus");
        harness.assertInGraveyard(player1, "Runeclaw Bear");
    }

    @Test
    @DisplayName("The end-of-combat sacrifice uses the stack and allows a response")
    void sacrificeWaitsForDelayedTriggerToResolve() {
        addCreatureReady(player1, new CrumblingColossus());

        harness.withAutoStop(TurnStep.END_OF_COMBAT, () -> {
            declareAttackers(List.of(0));
            harness.passUntil(TurnStep.END_OF_COMBAT);

            harness.assertLife(player2, 13);
            harness.assertOnBattlefield(player1, "Crumbling Colossus");
            assertThat(gd.stack).hasSize(1);

            harness.passBothPriorities();
            harness.assertInGraveyard(player1, "Crumbling Colossus");
        });
    }

    @Test
    @DisplayName("The delayed trigger cannot sacrifice Colossus controlled by another player")
    void cannotSacrificeAfterLosingControl() {
        Permanent colossus = addCreatureReady(player1, new CrumblingColossus());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.passBothPriorities();
        });

        // Model a control change after the attack trigger resolves; the creature leaves combat.
        gd.playerBattlefields.get(player1.getId()).remove(colossus);
        gd.playerBattlefields.get(player2.getId()).add(colossus);
        colossus.setAttacking(false);

        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        harness.assertOnBattlefield(player2, "Crumbling Colossus");
        harness.assertNotInGraveyard(player1, "Crumbling Colossus");
        harness.assertNotInGraveyard(player2, "Crumbling Colossus");
    }

    @Test
    @DisplayName("A blocked Colossus tramples over its blocker before being sacrificed")
    void blockedAttackerDealsTrampleDamageBeforeSacrifice() {
        addCreatureReady(player1, new CrumblingColossus());
        Permanent blocker = addCreatureReady(player2, new RuneclawBear());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
        });
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2,
                player2.getId(), 5
        ));
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        harness.assertLife(player2, 15);
        harness.assertInGraveyard(player2, "Runeclaw Bear");
        harness.assertInGraveyard(player1, "Crumbling Colossus");
    }
}