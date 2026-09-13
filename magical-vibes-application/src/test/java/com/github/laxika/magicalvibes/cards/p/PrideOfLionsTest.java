package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PrideOfLions.class, GrizzlyBears.class})
class PrideOfLionsTest extends BaseCardTest {

    @Test
    @DisplayName("Blocked Pride of Lions can assign combat damage to defending player")
    void blockedPrideOfLionsAssignsDamageToDefendingPlayer() {
        harness.setLife(player2, 20);
        Permanent prideOfLions = addCreatureReady(player1, new PrideOfLions());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        prideOfLions.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        // Assign all 4 damage to defending player (as though unblocked)
        harness.handleCombatDamageAssigned(player1, 0, Map.of(player2.getId(), 4));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        // Assigning damage as though unblocked does not prevent damage from the blocker.
        assertThat(prideOfLions.getMarkedDamage()).isEqualTo(2);
        // Blocker survives since no damage was assigned to it
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Blocked Pride of Lions can assign combat damage to blocker instead")
    void blockedPrideOfLionsAssignsDamageToBlocker() {
        harness.setLife(player2, 20);
        Permanent prideOfLions = addCreatureReady(player1, new PrideOfLions());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        prideOfLions.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        // Assign all damage to blocker instead of defending player
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 4));

        // Grizzly Bears (2/2) takes 4 damage → dies
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        // Life unchanged since damage went to blocker
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Blocked Pride of Lions cannot split damage between blocker and defending player")
    void cannotSplitAsThoughUnblockedCombatDamage() {
        harness.setLife(player2, 20);
        Permanent prideOfLions = addCreatureReady(player1, new PrideOfLions());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        prideOfLions.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThatThrownBy(() -> harness.handleCombatDamageAssigned(
                player1, 0, Map.of(blocker.getId(), 1, player2.getId(), 3)))
                .isInstanceOf(IllegalStateException.class);

        harness.handleCombatDamageAssigned(player1, 0, Map.of(player2.getId(), 4));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }
}
