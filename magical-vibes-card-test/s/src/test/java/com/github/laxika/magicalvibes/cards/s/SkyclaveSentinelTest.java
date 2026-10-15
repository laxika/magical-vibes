package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SkyclaveSentinel.class})
class SkyclaveSentinelTest extends BaseCardTest {

    @Test
    @DisplayName("Enters without counters when not kicked")
    void entersWithoutCountersWhenNotKicked() {
        harness.setHand(player1, List.of(new SkyclaveSentinel()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent sentinel = findPermanent(player1, "Skyclave Sentinel");
        assertThat(sentinel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Enters with two +1/+1 counters when kicked")
    void entersWithTwoCountersWhenKicked() {
        harness.setHand(player1, List.of(new SkyclaveSentinel()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        Permanent sentinel = findPermanent(player1, "Skyclave Sentinel");
        assertThat(sentinel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Only the counter-bearing sentinel can attack despite defender")
    void onlyCounterBearingSentinelCanAttackDespiteDefender() {
        Permanent sentinel = addCreatureReady(player1, new SkyclaveSentinel());
        harness.addToBattlefield(player2, new SkyclaveSentinel());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
        assertThat(sentinel.isAttacking()).isFalse();

        sentinel.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(0)));

        assertThat(sentinel.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Losing the last +1/+1 counter restores the defender restriction")
    void cannotAttackAfterLosingLastCounter() {
        Permanent sentinel = addCreatureReady(player1, new SkyclaveSentinel());
        sentinel.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        sentinel.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
        assertThat(sentinel.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Counters on another Sentinel do not allow this Sentinel to attack")
    void counterPermissionIsSpecificToItsSource() {
        Permanent sentinel = addCreatureReady(player1, new SkyclaveSentinel());
        Permanent other = addCreatureReady(player1, new SkyclaveSentinel());
        other.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
        assertThat(sentinel.isAttacking()).isFalse();

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(1)));
        assertThat(other.isAttacking()).isTrue();
        assertThat(sentinel.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("A counter of another type does not bypass defender")
    void otherCounterTypeDoesNotAllowAttacking() {
        Permanent sentinel = addCreatureReady(player1, new SkyclaveSentinel());
        sentinel.setCounterCount(CounterType.CHARGE, 1);

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
        assertThat(sentinel.isAttacking()).isFalse();
    }
}
