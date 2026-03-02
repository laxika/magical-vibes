package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FlensermiteTest extends BaseCardTest {

    // ===== Infect: combat damage to players gives poison counters =====

    @Test
    @DisplayName("Unblocked Flensermite deals poison counters to defending player")
    void unblockedDealsPoisonCounters() {
        Permanent flensermite = addFlensermiteReady(player1);
        flensermite.setAttacking(true);
        harness.setLife(player2, 20);

        resolveCombat();

        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(1);
        // Life should NOT change from infect damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    // ===== Infect: combat damage to creatures gives -1/-1 counters =====

    @Test
    @DisplayName("Flensermite deals -1/-1 counters to blocking creature")
    void dealsMinusCountersToBlocker() {
        Permanent flensermite = addFlensermiteReady(player1);
        flensermite.setAttacking(true);

        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        resolveCombat();

        // Grizzly Bears (2/2) gets 1 -1/-1 counter from 1-power Flensermite
        Permanent bears = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        assertThat(bears.getMinusOneMinusOneCounters()).isEqualTo(1);
    }

    // ===== Lifelink: controller gains life equal to damage dealt =====

    @Test
    @DisplayName("Unblocked Flensermite with lifelink gains controller life equal to poison damage")
    void lifelinkGainsLifeOnPoisonDamage() {
        Permanent flensermite = addFlensermiteReady(player1);
        flensermite.setAttacking(true);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        resolveCombat();

        // Lifelink still works with infect — controller gains 1 life
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        // Defender gets poison, not life loss
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Flensermite with lifelink gains controller life when dealing -1/-1 counters to blocker")
    void lifelinkGainsLifeOnCreatureDamage() {
        Permanent flensermite = addFlensermiteReady(player1);
        flensermite.setAttacking(true);
        harness.setLife(player1, 20);

        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        resolveCombat();

        // Lifelink still works when dealing -1/-1 counters to creatures
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    // ===== Helper methods =====

    private Permanent addFlensermiteReady(Player player) {
        Flensermite card = new Flensermite();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void resolveCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
