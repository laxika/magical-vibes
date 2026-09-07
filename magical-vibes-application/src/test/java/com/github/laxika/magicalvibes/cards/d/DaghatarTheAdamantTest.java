package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DaghatarTheAdamantTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with four +1/+1 counters")
    void entersWithFourPlusOnePlusOneCounters() {
        harness.setHand(player1, List.of(new DaghatarTheAdamant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent daghatar = findPermanent(player1, "Daghatar the Adamant");

        assertThat(daghatar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, daghatar)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, daghatar)).isEqualTo(4);
    }

    @Test
    @DisplayName("Moves one +1/+1 counter between two target creatures")
    void movesPlusOnePlusOneCounter() {
        harness.addToBattlefieldAndReturn(player1, new DaghatarTheAdamant());
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent destination = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        addActivationMana();

        harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(source.getId(), destination.getId()));
        harness.passBothPriorities();

        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(destination.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not move another counter kind")
    void doesNotMoveAnotherCounterKind() {
        harness.addToBattlefieldAndReturn(player1, new DaghatarTheAdamant());
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent destination = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        source.setCounterCount(CounterType.CHARGE, 1);
        addActivationMana();

        harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(source.getId(), destination.getId()));
        harness.passBothPriorities();

        assertThat(source.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(destination.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Requires two different creature targets")
    void requiresDifferentCreatureTargets() {
        harness.addToBattlefieldAndReturn(player1, new DaghatarTheAdamant());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Rejects a noncreature target")
    void rejectsNonCreatureTarget() {
        harness.addToBattlefieldAndReturn(player1, new DaghatarTheAdamant());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(creature.getId(), land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);
    }
}
