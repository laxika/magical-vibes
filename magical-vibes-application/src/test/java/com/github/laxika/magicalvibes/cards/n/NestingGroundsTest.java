package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MelirasKeepers;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NestingGrounds.class, GrizzlyBears.class, Forest.class, MelirasKeepers.class})
class NestingGroundsTest extends BaseCardTest {

    @Test
    @DisplayName("Moves one counter from a permanent you control onto a second permanent")
    void movesCounterBetweenPermanents() {
        Permanent grounds = addReadyGrounds(player1);
        Permanent source = addCreatureReady(player1, new GrizzlyBears());
        Permanent destination = addCreatureReady(player2, new Forest());
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        activateMoveAbility(source, destination);

        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(destination.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(grounds.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Requires the first target permanent to be controlled by the ability controller")
    void rejectsFirstTargetNotControlled() {
        Permanent grounds = addReadyGrounds(player1);
        Permanent source = addCreatureReady(player2, new GrizzlyBears());
        Permanent destination = addCreatureReady(player1, new Forest());
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        prepareActivation(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 1, List.of(source.getId(), destination.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(grounds.isTapped()).isFalse();
        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Can be activated only at sorcery speed")
    void onlyActivatesAtSorcerySpeed() {
        Permanent grounds = addReadyGrounds(player1);
        Permanent source = addCreatureReady(player1, new GrizzlyBears());
        Permanent destination = addCreatureReady(player2, new Forest());
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 1, List.of(source.getId(), destination.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(grounds.isTapped()).isFalse();
        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void producesColorlessManaImmediately() {
        Permanent grounds = addReadyGrounds(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(grounds.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void choosesCounterKindOnResolution() {
        Permanent source = addReadyGrounds(player1);
        Permanent destination = addReadyGrounds(player2);
        source.setCounterCount(CounterType.CHARGE, 1);
        source.setCounterCount(CounterType.TIME, 1);

        activateMoveAbility(source, destination);

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(source.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(source.getCounterCount(CounterType.TIME)).isEqualTo(1);
        assertThat(destination.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(destination.getCounterCount(CounterType.TIME)).isZero();
    }

    @Test
    @CardUsed({MelirasKeepers.class})
    void doesNotRemoveCounterWhenDestinationCannotReceiveIt() {
        Permanent source = addReadyGrounds(player1);
        Permanent destination = harness.addToBattlefieldAndReturn(player2, new MelirasKeepers());
        source.setCounterCount(CounterType.CHARGE, 1);

        activateMoveAbility(source, destination);

        assertThat(source.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(destination.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    void resolvesWithoutMovingAnythingWhenSourceHasNoCounters() {
        Permanent source = addReadyGrounds(player1);
        Permanent destination = addReadyGrounds(player2);

        activateMoveAbility(source, destination);

        assertThat(source.getCounters()).isEmpty();
        assertThat(destination.getCounters()).isEmpty();
        assertThat(source.isTapped()).isTrue();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void rejectsSamePermanentForBothTargets() {
        Permanent source = addReadyGrounds(player1);
        source.setCounterCount(CounterType.CHARGE, 1);
        prepareActivation(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 1, List.of(source.getId(), source.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(source.isTapped()).isFalse();
        assertThat(source.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    void doesNotMoveCounterWhenSourceChangesControllerBeforeResolution() {
        Permanent source = addReadyGrounds(player1);
        Permanent destination = addReadyGrounds(player2);
        source.setCounterCount(CounterType.CHARGE, 1);
        prepareActivation(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbilityWithMultiTargets(player1, 0, 1, List.of(source.getId(), destination.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerBattlefields.get(player2.getId()).add(source);

        harness.passBothPriorities();

        assertThat(source.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(destination.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    void doesNotRemoveCounterWhenDestinationLeavesBattlefield() {
        Permanent source = addReadyGrounds(player1);
        Permanent destination = addReadyGrounds(player2);
        source.setCounterCount(CounterType.CHARGE, 1);
        prepareActivation(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbilityWithMultiTargets(player1, 0, 1, List.of(source.getId(), destination.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(destination);
        gd.playerGraveyards.get(player2.getId()).add(destination.getCard());

        harness.passBothPriorities();

        assertThat(source.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(destination.getCounterCount(CounterType.CHARGE)).isZero();
    }

    private Permanent addReadyGrounds(Player player) {
        return addCreatureReady(player, new NestingGrounds());
    }

    private void activateMoveAbility(Permanent source, Permanent destination) {
        prepareActivation(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbilityWithMultiTargets(player1, 0, 1, List.of(source.getId(), destination.getId()));
        harness.passBothPriorities();
    }

    private void prepareActivation(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
