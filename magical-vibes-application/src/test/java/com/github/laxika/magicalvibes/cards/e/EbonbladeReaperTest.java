package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(EbonbladeReaper.class)
class EbonbladeReaperTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking makes its controller lose half their life, rounded up")
    void attackingMakesControllerLoseHalfLife() {
        harness.setLife(player1, 21);
        addCreatureReady(player1, new EbonbladeReaper());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("Combat damage makes the damaged player lose half their life, rounded up")
    void combatDamageMakesDamagedPlayerLoseHalfLife() {
        harness.setLife(player2, 22);
        Permanent reaper = addCreatureReady(player1, new EbonbladeReaper());
        reaper.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("A blocked attack does not cause the damaged-player trigger")
    void blockedAttackDoesNotMakeDefendingPlayerLoseHalfLife() {
        harness.setLife(player2, 22);
        Permanent reaper = addCreatureReady(player1, new EbonbladeReaper());
        reaper.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new EbonbladeReaper());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(22);
    }
}
