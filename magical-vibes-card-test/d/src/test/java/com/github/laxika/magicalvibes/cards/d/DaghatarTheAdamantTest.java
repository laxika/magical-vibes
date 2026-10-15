package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.ArashinCleric;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DaghatarTheAdamant.class, GrizzlyBears.class, Forest.class, ArashinCleric.class})
class DaghatarTheAdamantTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with four +1/+1 counters")
    void entersWithFourPlusOnePlusOneCounters() {
        harness.castFromHand(player1, new DaghatarTheAdamant(), "{3}{W}");
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

    @Test
    @DisplayName("Vigilance keeps Daghatar untapped when attacking")
    void attacksWithoutTapping() {
        Permanent daghatar = harness.enterBattlefieldAndReturn(player1, new DaghatarTheAdamant());
        daghatar.setSummoningSick(false);

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(daghatar.isAttacking()).isTrue();
        assertThat(daghatar.isTapped()).isFalse();
    }

    @ParameterizedTest
    @CsvSource({"2, 0", "1, 1", "0, 2"})
    @DisplayName("A tapped, summoning-sick Daghatar can use either color for each hybrid symbol")
    void paysHybridManaAndMovesExactlyOneCounter(int black, int green) {
        Permanent daghatar = harness.enterBattlefieldAndReturn(player1, new DaghatarTheAdamant());
        daghatar.tap();
        daghatar.setSummoningSick(true);
        Permanent source = harness.addToBattlefieldAndReturn(player2, new ArashinCleric());
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, black);
        harness.addMana(player1, ManaColor.GREEN, green);

        harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(source.getId(), daghatar.getId()));
        harness.passBothPriorities();

        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(daghatar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        assertThat(daghatar.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A creature without counters is a legal source but nothing is moved")
    void noCounterOnSourceDoesNothing() {
        Permanent daghatar = harness.enterBattlefieldAndReturn(player1, new DaghatarTheAdamant());
        Permanent source = harness.addToBattlefieldAndReturn(player2, new ArashinCleric());
        addActivationMana();

        harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(source.getId(), daghatar.getId()));
        harness.passBothPriorities();

        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(daghatar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @DisplayName("No counter is moved when either target leaves before resolution")
    void targetLeavesBeforeResolution(boolean sourceLeaves) {
        harness.enterBattlefieldAndReturn(player1, new DaghatarTheAdamant());
        Permanent source = harness.addToBattlefieldAndReturn(player2, new ArashinCleric());
        Permanent destination = harness.addToBattlefieldAndReturn(player2, new ArashinCleric());
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        addActivationMana();
        harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(source.getId(), destination.getId()));

        gd.playerBattlefields.get(player2.getId()).remove(sourceLeaves ? source : destination);
        harness.passBothPriorities();

        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(destination.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The counter must still exist when the ability resolves")
    void sourceLosesItsCounterBeforeResolution() {
        Permanent daghatar = harness.enterBattlefieldAndReturn(player1, new DaghatarTheAdamant());
        Permanent source = harness.addToBattlefieldAndReturn(player2, new ArashinCleric());
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        addActivationMana();
        harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(source.getId(), daghatar.getId()));

        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        harness.passBothPriorities();

        assertThat(daghatar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Moving Daghatar's last counter succeeds before it dies")
    void movesLastCounterBeforeDying() {
        Permanent daghatar = harness.enterBattlefieldAndReturn(player1, new DaghatarTheAdamant());
        daghatar.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent destination = harness.addToBattlefieldAndReturn(player1, new ArashinCleric());
        addActivationMana();

        harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(daghatar.getId(), destination.getId()));
        harness.passBothPriorities();

        assertThat(destination.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Daghatar the Adamant");
        harness.assertInGraveyard(player1, "Daghatar the Adamant");
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);
    }
}
