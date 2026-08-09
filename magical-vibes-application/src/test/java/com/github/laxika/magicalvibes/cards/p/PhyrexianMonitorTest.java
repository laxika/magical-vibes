package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PhyrexianMonitorTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the activated ability grants a regeneration shield")
    void regenerationAbilityGrantsShield() {
        Permanent monitor = addReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(monitor.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Regeneration shield saves Phyrexian Monitor from lethal combat damage")
    void regenerationSavesFromLethalCombat() {
        Permanent monitor = addReady(player1);
        monitor.setRegenerationShield(1);
        monitor.setBlocking(true);
        monitor.addBlockingTarget(0);

        Permanent attacker = addAttacker(player2);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Phyrexian Monitor");
        assertThat(monitor.isTapped()).isTrue();
        assertThat(monitor.getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("Phyrexian Monitor dies without a regeneration shield")
    void diesWithoutRegenerationShield() {
        Permanent monitor = addReady(player1);
        monitor.setBlocking(true);
        monitor.addBlockingTarget(0);

        Permanent attacker = addAttacker(player2);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Phyrexian Monitor");
        harness.assertInGraveyard(player1, "Phyrexian Monitor");
    }

    private Permanent addReady(Player player) {
        Permanent perm = new Permanent(new PhyrexianMonitor());
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addAttacker(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
