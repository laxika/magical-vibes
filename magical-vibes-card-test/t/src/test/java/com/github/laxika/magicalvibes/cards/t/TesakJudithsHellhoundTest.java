package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.Dog;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TesakJudithsHellhound.class, Dog.class, GrizzlyBears.class})
class TesakJudithsHellhoundTest extends BaseCardTest {

    @Test
    @DisplayName("Tesak's unleash can put a +1/+1 counter on it")
    void tesakCanUnleash() {
        Permanent tesak = enterTesak(true);

        assertThat(tesak.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Other Dogs you control can unleash and then can't block")
    void otherDogsGainUnleash() {
        addCreatureReady(player1, new TesakJudithsHellhound());
        Permanent dog = harness.enterBattlefieldAndReturn(player1, new Dog());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(dog.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        addCreatureReady(player2, new GrizzlyBears());
        declareAttackersAndPrepareBlockers(player2, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(1, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Creatures you control with counters have haste")
    void counteredCreatureHasHaste() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new TesakJudithsHellhound());
        Permanent dog = new Permanent(new Dog());
        dog.setSummoningSick(true);
        dog.setCounterCount(CounterType.CHARGE, 1);
        gd.playerBattlefields.get(player1.getId()).add(dog);

        declareAttackers(player1, List.of(1));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Attacking with Tesak adds red mana for each attacking creature")
    void attackAddsManaForEachAttackingCreature() {
        addCreatureReady(player1, new TesakJudithsHellhound());
        addCreatureReady(player1, new GrizzlyBears());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player1, List.of(0, 1));
            resolveAllTriggers();
        });

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.RED)).isEqualTo(2);
    }

    @Test
    @DisplayName("Tesak can decline unleash and block without a +1/+1 counter")
    void declinedUnleashAllowsBlocking() {
        Permanent tesak = enterTesak(false);
        assertThat(tesak.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        addCreatureReady(player2, new GrizzlyBears());
        declareAttackersAndPrepareBlockers(player2, List.of(0));

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0))));

        assertThat(tesak.getBlockingTargets()).contains(0);
    }

    @Test
    @DisplayName("A +1/+1 counter from any source prevents Tesak from blocking")
    void externallyCounteredTesakCannotBlock() {
        Permanent tesak = enterTesak(false);
        tesak.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        addCreatureReady(player2, new GrizzlyBears());
        declareAttackersAndPrepareBlockers(player2, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Tesak prevents a Dog with an externally placed +1/+1 counter from blocking")
    void externallyCounteredDogCannotBlock() {
        addCreatureReady(player1, new TesakJudithsHellhound());
        Permanent dog = harness.addToBattlefieldAndReturn(player1, new Dog());
        dog.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        addCreatureReady(player2, new GrizzlyBears());
        declareAttackersAndPrepareBlockers(player2, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(1, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An opponent's Dog enters without receiving unleash from Tesak")
    void opponentDogDoesNotGainUnleash() {
        addCreatureReady(player1, new TesakJudithsHellhound());

        Permanent dog = harness.enterBattlefieldAndReturn(player2, new Dog());

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(dog);
        assertThat(dog.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Charge counters do not prevent a Dog with unleash from blocking")
    void dogWithOnlyChargeCounterCanBlock() {
        addCreatureReady(player1, new TesakJudithsHellhound());
        Permanent dog = harness.addToBattlefieldAndReturn(player1, new Dog());
        dog.setCounterCount(CounterType.CHARGE, 1);
        addCreatureReady(player2, new GrizzlyBears());
        declareAttackersAndPrepareBlockers(player2, List.of(0));

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(1, 0))));

        assertThat(dog.getBlockingTargets()).contains(0);
    }

    @Test
    @DisplayName("A Dog loses its granted blocking restriction when Tesak leaves")
    void dogCanBlockAfterTesakLeaves() {
        Permanent tesak = addCreatureReady(player1, new TesakJudithsHellhound());
        Permanent dog = harness.addToBattlefieldAndReturn(player1, new Dog());
        dog.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        gd.playerBattlefields.get(player1.getId()).remove(tesak);
        addCreatureReady(player2, new GrizzlyBears());
        declareAttackersAndPrepareBlockers(player2, List.of(0));

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0))));

        assertThat(dog.getBlockingTargets()).contains(0);
    }

    @Test
    @DisplayName("Tesak grants haste to itself when unleashed")
    void unleashedTesakCanAttackImmediately() {
        enterTesak(true);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));

        assertThat(findPermanent(player1, "Tesak, Judith's Hellhound").isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Countered non-Dog creatures also have haste")
    void counteredNonDogCanAttackImmediately() {
        addCreatureReady(player1, new TesakJudithsHellhound());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setSummoningSick(true);
        bears.setCounterCount(CounterType.CHARGE, 1);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(1)));

        assertThat(bears.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Creatures without counters do not receive haste")
    void uncounteredDogCannotAttackImmediately() {
        addCreatureReady(player1, new TesakJudithsHellhound());
        Permanent dog = harness.addToBattlefieldAndReturn(player1, new Dog());
        dog.setSummoningSick(true);

        assertThatThrownBy(() -> declareAttackers(List.of(1)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Opponents' countered creatures do not receive haste")
    void opponentCounteredCreatureCannotAttackImmediately() {
        addCreatureReady(player1, new TesakJudithsHellhound());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setSummoningSick(true);
        bears.setCounterCount(CounterType.CHARGE, 1);

        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Tesak counts creatures still attacking when its trigger resolves")
    void attackManaUsesResolutionCountAfterTesakLeaves() {
        Permanent tesak = addCreatureReady(player1, new TesakJudithsHellhound());
        addCreatureReady(player1, new GrizzlyBears());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0, 1));
            gd.playerBattlefields.get(player1.getId()).remove(tesak);
            resolveAllTriggers();
        });

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    private Permanent enterTesak(boolean unleash) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new TesakJudithsHellhound(), "{3}{R}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, unleash);
        return findPermanent(player1, "Tesak, Judith's Hellhound");
    }
}
