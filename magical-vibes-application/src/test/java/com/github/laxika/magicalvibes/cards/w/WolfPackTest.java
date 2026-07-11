package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class WolfPackTest extends BaseCardTest {

    @Test
    @DisplayName("Blocked Wolf Pack can assign combat damage to defending player")
    void blockedWolfPackAssignsDamageToDefendingPlayer() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new WolfPack());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent wolfPack = gd.playerBattlefields.get(player1.getId()).getFirst();
        wolfPack.setSummoningSick(false);
        wolfPack.setAttacking(true);

        Permanent blocker = gd.playerBattlefields.get(player2.getId()).getFirst();
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // Assign all 7 damage to defending player (as though unblocked)
        harness.handleCombatDamageAssigned(player1, 0, Map.of(player2.getId(), 7));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(13);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Blocked Wolf Pack can assign combat damage to blocker instead")
    void blockedWolfPackAssignsDamageToBlocker() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new WolfPack());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent wolfPack = gd.playerBattlefields.get(player1.getId()).getFirst();
        wolfPack.setSummoningSick(false);
        wolfPack.setAttacking(true);

        Permanent blocker = gd.playerBattlefields.get(player2.getId()).getFirst();
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // Assign all damage to blocker instead of defending player
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 7));

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
