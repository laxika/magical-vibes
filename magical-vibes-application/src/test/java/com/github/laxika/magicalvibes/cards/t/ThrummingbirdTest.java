package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Thrummingbird.class, GrizzlyBears.class, SerraAngel.class})
class ThrummingbirdTest extends BaseCardTest {

    private Permanent addReadyThrummingbird() {
        return addCreatureReady(player1, new Thrummingbird());
    }

    @Test
    @DisplayName("Dealing combat damage triggers proliferate and adds -1/-1 counter to chosen creature")
    void proliferateOnCombatDamage() {
        Permanent bird = addReadyThrummingbird();
        bird.setAttacking(true);

        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        resolveCombat();
        resolveAllTriggers();

        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        assertThat(bears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Dealing combat damage triggers proliferate and adds +1/+1 counter to chosen creature")
    void proliferateAddsPlusCounters() {
        Permanent bird = addReadyThrummingbird();
        bird.setAttacking(true);

        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        resolveCombat();
        resolveAllTriggers();

        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Proliferate can choose no permanents")
    void proliferateCanChooseNone() {
        Permanent bird = addReadyThrummingbird();
        bird.setAttacking(true);

        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        resolveCombat();
        resolveAllTriggers();

        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(bears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Proliferate can add counters to multiple permanents")
    void proliferateMultiplePermanents() {
        Permanent bird = addReadyThrummingbird();
        bird.setAttacking(true);

        Permanent bears1 = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears1.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        Permanent bears2 = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears2.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        resolveCombat();
        resolveAllTriggers();

        harness.handleMultiplePermanentsChosen(player1, List.of(bears1.getId(), bears2.getId()));

        assertThat(bears1.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(bears2.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("No proliferate trigger when blocked")
    void noTriggerWhenBlocked() {
        Permanent bird = addReadyThrummingbird();
        bird.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new SerraAngel());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0); // Thrummingbird is at index 0

        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        resolveCombat();
        resolveAllTriggers();

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
        resolveAllTriggers();

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
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }
    @Test
    @DisplayName("Proliferate adds each existing counter kind and leaves unchosen permanents unchanged")
    void proliferateEveryCounterKind() {
        Permanent bird = addReadyThrummingbird();
        bird.setAttacking(true);
        Permanent chosen = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        chosen.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        chosen.setCounterCount(CounterType.CHARGE, 3);
        Permanent unchosen = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        unchosen.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        resolveCombat();
        resolveAllTriggers();
        harness.handleMultiplePermanentsChosen(player1, List.of(chosen.getId()));

        assertThat(chosen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(chosen.getCounterCount(CounterType.CHARGE)).isEqualTo(4);
        assertThat(chosen.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(unchosen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Proliferate can choose both players together with a permanent")
    void proliferatePlayersAndPermanent() {
        Permanent bird = addReadyThrummingbird();
        bird.setAttacking(true);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        gd.playerPoisonCounters.put(player1.getId(), 1);
        gd.playerPoisonCounters.put(player2.getId(), 2);

        resolveCombat();
        resolveAllTriggers();
        harness.handleMultiplePermanentsChosen(player1,
                List.of(player1.getId(), player2.getId(), bears.getId()));

        assertThat(gd.playerPoisonCounters.get(player1.getId())).isEqualTo(2);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(3);
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Proliferate can choose only one player when no permanent has counters")
    void proliferateOnlyChosenPlayer() {
        Permanent bird = addReadyThrummingbird();
        bird.setAttacking(true);
        gd.playerPoisonCounters.put(player1.getId(), 1);
        gd.playerPoisonCounters.put(player2.getId(), 2);

        resolveCombat();
        resolveAllTriggers();
        harness.handleMultiplePermanentsChosen(player1, List.of(player2.getId()));

        assertThat(gd.playerPoisonCounters.get(player1.getId())).isEqualTo(1);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(3);
        assertThat(bird.getCounters()).isEmpty();
    }
}
