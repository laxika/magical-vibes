package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import com.github.laxika.magicalvibes.model.CounterType;

class ThrummingbirdTest extends BaseCardTest {

    private Permanent addReadyThrummingbird() {
        Permanent perm = new Permanent(new Thrummingbird());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(perm);
        return perm;
    }

    private void resolveCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    // ===== Combat damage triggers proliferate =====

    @Test
    @DisplayName("Dealing combat damage triggers proliferate and adds -1/-1 counter to chosen creature")
    void proliferateOnCombatDamage() {
        Permanent bird = addReadyThrummingbird();
        bird.setAttacking(true);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        resolveCombat();

        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        assertThat(bears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Dealing combat damage triggers proliferate and adds +1/+1 counter to chosen creature")
    void proliferateAddsPlusCounters() {
        Permanent bird = addReadyThrummingbird();
        bird.setAttacking(true);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        resolveCombat();

        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Proliferate can choose no permanents")
    void proliferateCanChooseNone() {
        Permanent bird = addReadyThrummingbird();
        bird.setAttacking(true);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        resolveCombat();

        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(bears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Proliferate can add counters to multiple permanents")
    void proliferateMultiplePermanents() {
        Permanent bird = addReadyThrummingbird();
        bird.setAttacking(true);

        Permanent bears1 = new Permanent(new GrizzlyBears());
        bears1.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        gd.playerBattlefields.get(player1.getId()).add(bears1);

        Permanent bears2 = new Permanent(new GrizzlyBears());
        bears2.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        gd.playerBattlefields.get(player2.getId()).add(bears2);

        resolveCombat();

        harness.handleMultiplePermanentsChosen(player1, List.of(bears1.getId(), bears2.getId()));

        assertThat(bears1.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(bears2.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("No proliferate trigger when blocked")
    void noTriggerWhenBlocked() {
        Permanent bird = addReadyThrummingbird();
        bird.setAttacking(true);

        Permanent blocker = new Permanent(new SerraAngel());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0); // Thrummingbird is at index 0

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        resolveCombat();

        // No proliferate trigger — bears counter unchanged
        assertThat(bears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("No proliferate choice when no permanents have counters")
    void noProliferateWithoutEligiblePermanents() {
        Permanent bird = addReadyThrummingbird();
        bird.setAttacking(true);
        harness.setLife(player2, 20);

        harness.addToBattlefield(player2, new GrizzlyBears());

        resolveCombat();

        // Proliferate resolves with no eligible permanents — no choice needed
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Defender takes 1 combat damage from Thrummingbird")
    void defenderTakesCombatDamage() {
        Permanent bird = addReadyThrummingbird();
        bird.setAttacking(true);
        harness.setLife(player2, 20);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }
}
