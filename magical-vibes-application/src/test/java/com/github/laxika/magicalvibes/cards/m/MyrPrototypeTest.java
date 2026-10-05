package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AlphaMyr;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MyrPrototype.class, AlphaMyr.class})
class MyrPrototypeTest extends BaseCardTest {

    @Test
    @DisplayName("Gains a +1/+1 counter at the beginning of its controller's upkeep")
    void gainsCounterOnUpkeep() {
        Permanent myr = addCreatureReady(player1, new MyrPrototype());

        advanceToUpkeep(player1);

        harness.passBothPriorities();

        assertThat(myr.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not gain a counter during an opponent's upkeep")
    void doesNotGainCounterDuringOpponentsUpkeep() {
        Permanent myr = addCreatureReady(player1, new MyrPrototype());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(myr.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Cannot attack without paying for each +1/+1 counter")
    void attackRequiresManaForEachCounter() {
        Permanent myr = addCreatureReady(player1, new MyrPrototype());
        myr.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setLife(player2, 20);

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana to pay attack tax (2 required)");

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        declareAttackers(player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isLessThan(20);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Can attack without mana when it has no +1/+1 counters")
    void attackWithNoCountersIsFree() {
        Permanent myr = addCreatureReady(player1, new MyrPrototype());
        harness.setLife(player2, 20);

        declareAttackers(player1, List.of(0));

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(myr.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot block without paying for each +1/+1 counter")
    void blockRequiresManaForEachCounter() {
        Permanent attacker = addCreatureReady(player1, new AlphaMyr());
        Permanent myr = addCreatureReady(player2, new MyrPrototype());
        myr.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        declareAttackersAndPrepareBlockers(player1, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana to pay block cost (2 required)");

        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(myr.isBlocking()).isTrue();
        assertThat(attacker.isAttacking()).isTrue();
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Can block without mana when it has no +1/+1 counters")
    void blockWithNoCountersIsFree() {
        Permanent attacker = addCreatureReady(player1, new AlphaMyr());
        Permanent myr = addCreatureReady(player2, new MyrPrototype());

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(myr.isBlocking()).isTrue();
        assertThat(attacker.isAttacking()).isTrue();
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Each Prototype receives its own upkeep counter")
    void eachPrototypeGetsAnUpkeepCounter() {
        Permanent first = addCreatureReady(player1, new MyrPrototype());
        Permanent second = addCreatureReady(player1, new MyrPrototype());
        first.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Attacking with two Prototypes pays the sum of their counter costs")
    void attackCostsAreSummedAcrossPrototypes() {
        Permanent first = addCreatureReady(player1, new MyrPrototype());
        Permanent second = addCreatureReady(player1, new MyrPrototype());
        first.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        second.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0, 1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana to pay attack tax (3 required)");
        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);

        harness.addMana(player1, ManaColor.RED, 1);
        declareAttackers(player1, List.of(0, 1));

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Counters other than +1/+1 counters do not impose combat costs")
    void unrelatedCountersDoNotRequireCombatPayment() {
        Permanent attackingMyr = addCreatureReady(player1, new MyrPrototype());
        Permanent blockingMyr = addCreatureReady(player2, new MyrPrototype());
        attackingMyr.setCounterCount(CounterType.CHARGE, 3);
        blockingMyr.setCounterCount(CounterType.CHARGE, 4);

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(attackingMyr.isAttacking()).isTrue();
        assertThat(blockingMyr.isBlocking()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }
}
