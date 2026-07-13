package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BoggartRamGangTest extends BaseCardTest {

    // ===== Wither: combat damage to a creature is dealt as -1/-1 counters =====

    @Test
    @DisplayName("Blocked Ram-Gang deals -1/-1 counters to the blocker instead of regular damage")
    void witherDealsMinusCountersToBlocker() {
        // Grizzly Bears is 2/2
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        // Boggart Ram-Gang is 3/3 with wither + haste
        Permanent attacker = addCreatureReady(player1, new BoggartRamGang());
        attacker.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        // 3 wither damage = three -1/-1 counters → 2/2 Grizzly Bears becomes 0/0 and dies
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");

        // Ram-Gang (3/3) took only 2 regular damage and survives
        harness.assertOnBattlefield(player1, "Boggart Ram-Gang");
    }

    // ===== Wither to a player is ordinary life loss (no poison) =====

    @Test
    @DisplayName("Unblocked Ram-Gang deals ordinary combat damage to the player")
    void witherDealsLifeLossToPlayer() {
        harness.setLife(player2, 20);

        Permanent attacker = addCreatureReady(player1, new BoggartRamGang());
        attacker.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Wither only affects creatures; players take normal damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
    }
}
