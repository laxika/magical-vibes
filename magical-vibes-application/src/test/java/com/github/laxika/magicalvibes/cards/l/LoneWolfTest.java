package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LoneWolf.class, GrizzlyBears.class})
class LoneWolfTest extends BaseCardTest {

    @Test
    @DisplayName("Blocked Lone Wolf can assign combat damage to defending player")
    void blockedLoneWolfAssignsDamageToDefendingPlayer() {
        harness.setLife(player2, 20);
        Permanent loneWolf = addCreatureReady(player1, new LoneWolf());
        harness.addToBattlefield(player2, new GrizzlyBears());

        loneWolf.setAttacking(true);

        Permanent blocker = findPermanent(player2, "Grizzly Bears");
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        // Assign all 2 damage to defending player (as though unblocked)
        harness.handleCombatDamageAssigned(player1, 0, Map.of(player2.getId(), 2));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        // Blocker survives since no damage was assigned to it
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Blocked Lone Wolf can assign combat damage to blocker instead")
    void blockedLoneWolfAssignsDamageToBlocker() {
        harness.setLife(player2, 20);
        Permanent loneWolf = addCreatureReady(player1, new LoneWolf());
        harness.addToBattlefield(player2, new GrizzlyBears());

        loneWolf.setAttacking(true);

        Permanent blocker = findPermanent(player2, "Grizzly Bears");
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        // Assign both damage to blocker instead of defending player
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 2));

        // Grizzly Bears (2/2) takes 2 damage → dies
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        // Life unchanged since damage went to blocker
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Blocked Lone Wolf can assign combat damage to defending player with multiple blockers")
    void blockedLoneWolfAssignsDamageToDefendingPlayerWithMultipleBlockers() {
        harness.setLife(player2, 20);
        Permanent loneWolf = addCreatureReady(player1, new LoneWolf());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        loneWolf.setAttacking(true);

        List<Permanent> blockers = findPermanents(player2, "Grizzly Bears");
        blockers.forEach(blocker -> {
            blocker.setBlocking(true);
            blocker.addBlockingTarget(0);
        });

        resolveCombat();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(player2.getId(), 2));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(findPermanents(player2, "Grizzly Bears")).hasSize(2);
    }

    @Test
    @DisplayName("Unblocked Lone Wolf deals combat damage to defending player normally")
    void unblockedLoneWolfDealsCombatDamageNormally() {
        harness.setLife(player2, 20);
        Permanent loneWolf = addCreatureReady(player1, new LoneWolf());
        loneWolf.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Blocked Lone Wolf cannot split damage between blocker and defending player")
    void blockedLoneWolfCannotSplitDamageBetweenBlockerAndDefendingPlayer() {
        harness.setLife(player2, 20);
        Permanent loneWolf = addCreatureReady(player1, new LoneWolf());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        loneWolf.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThatThrownBy(() -> harness.handleCombatDamageAssigned(
                player1, 0, Map.of(blocker.getId(), 1, player2.getId(), 1)))
                .isInstanceOf(IllegalStateException.class);
    }
}
