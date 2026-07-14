package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class PrideOfLionsTest extends BaseCardTest {

    @Test
    @DisplayName("Blocked Pride of Lions can assign combat damage to defending player")
    void blockedPrideOfLionsAssignsDamageToDefendingPlayer() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new PrideOfLions());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent prideOfLions = gd.playerBattlefields.get(player1.getId()).getFirst();
        prideOfLions.setSummoningSick(false);
        prideOfLions.setAttacking(true);

        Permanent blocker = gd.playerBattlefields.get(player2.getId()).getFirst();
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // Assign all 4 damage to defending player (as though unblocked)
        harness.handleCombatDamageAssigned(player1, 0, Map.of(player2.getId(), 4));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        // Blocker survives since no damage was assigned to it
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Blocked Pride of Lions can assign combat damage to blocker instead")
    void blockedPrideOfLionsAssignsDamageToBlocker() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new PrideOfLions());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent prideOfLions = gd.playerBattlefields.get(player1.getId()).getFirst();
        prideOfLions.setSummoningSick(false);
        prideOfLions.setAttacking(true);

        Permanent blocker = gd.playerBattlefields.get(player2.getId()).getFirst();
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // Assign all damage to blocker instead of defending player
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 4));

        // Grizzly Bears (2/2) takes 4 damage → dies
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        // Life unchanged since damage went to blocker
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
