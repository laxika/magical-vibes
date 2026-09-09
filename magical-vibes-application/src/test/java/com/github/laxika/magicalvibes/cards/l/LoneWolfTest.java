package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.b.BloatedToad;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LoneWolf.class, BloatedToad.class})
class LoneWolfTest extends BaseCardTest {

    @Test
    @DisplayName("Blocked Lone Wolf can assign combat damage to defending player")
    void blockedLoneWolfAssignsDamageToDefendingPlayer() {
        harness.setLife(player2, 20);
        Permanent loneWolf = addCreatureReady(player1, new LoneWolf());
        Permanent blocker = addCreatureReady(player2, new BloatedToad());

        loneWolf.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        // Assign all 2 damage to defending player (as though unblocked)
        harness.handleCombatDamageAssigned(player1, 0, Map.of(player2.getId(), 2));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        // Blocker survives since no damage was assigned to it
        harness.assertOnBattlefield(player2, "Bloated Toad");
    }

    @Test
    @DisplayName("Blocked Lone Wolf can assign combat damage to blocker instead")
    void blockedLoneWolfAssignsDamageToBlocker() {
        harness.setLife(player2, 20);
        Permanent loneWolf = addCreatureReady(player1, new LoneWolf());
        Permanent blocker = addCreatureReady(player2, new BloatedToad());

        loneWolf.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        // Assign both damage to blocker instead of defending player
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 2));

        // Bloated Toad (2/2) takes 2 damage and dies
        harness.assertNotOnBattlefield(player2, "Bloated Toad");
        harness.assertInGraveyard(player2, "Bloated Toad");
        // Life unchanged since damage went to blocker
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Lone Wolf cannot split combat damage between its blocker and the player")
    void cannotSplitDamageBetweenBlockerAndPlayer() {
        harness.setLife(player2, 20);
        Permanent loneWolf = addCreatureReady(player1, new LoneWolf());
        Permanent blocker = addCreatureReady(player2, new BloatedToad());

        loneWolf.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThatThrownBy(() -> harness.handleCombatDamageAssigned(player1, 0,
                Map.of(blocker.getId(), 1, player2.getId(), 1)))
                .isInstanceOf(IllegalStateException.class);
    }
}
