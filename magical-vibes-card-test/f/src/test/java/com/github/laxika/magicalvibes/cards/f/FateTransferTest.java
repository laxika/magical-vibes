package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.CrabappleCohort;
import com.github.laxika.magicalvibes.cards.t.Tatterkite;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FateTransfer.class, CrabappleCohort.class, Forest.class, Tatterkite.class})
class FateTransferTest extends BaseCardTest {

    @Test
    @DisplayName("Moves all counters of every kind from the first target creature onto the second")
    void movesAllCounters() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new CrabappleCohort());
        Permanent destination = harness.addToBattlefieldAndReturn(player1, new CrabappleCohort());
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        source.setCounterCount(CounterType.CHARGE, 2);

        cast(source, destination);

        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
        assertThat(source.getCounterCount(CounterType.CHARGE)).isEqualTo(0);
        assertThat(destination.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(destination.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Moved counters are added to counters already on the destination")
    void addsToExistingCountersOnDestination() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new CrabappleCohort());
        Permanent destination = harness.addToBattlefieldAndReturn(player1, new CrabappleCohort());
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        destination.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        cast(source, destination);

        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
        assertThat(destination.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does nothing when the first target creature has no counters")
    void noOpWhenSourceHasNoCounters() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new CrabappleCohort());
        Permanent destination = harness.addToBattlefieldAndReturn(player1, new CrabappleCohort());

        cast(source, destination);

        assertThat(destination.getCounters().values().stream().anyMatch(v -> v > 0)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void rejectsNonCreatureTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CrabappleCohort());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        prepareSpell();

        assertThatThrownBy(() ->
                harness.castInstant(player1, 0, List.of(creature.getId(), land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Counters stay on the source when the destination cannot receive counters")
    void cannotMoveCountersOntoTatterkite() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new CrabappleCohort());
        Permanent destination = harness.addToBattlefieldAndReturn(player2, new Tatterkite());
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        source.setCounterCount(CounterType.CHARGE, 3);

        cast(source, destination);

        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(source.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
        assertThat(destination.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(destination.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    @DisplayName("Moves negative counters across controllers and cancels opposing counters")
    void movesNegativeCountersAcrossControllers() {
        Permanent source = harness.addToBattlefieldAndReturn(player2, new CrabappleCohort());
        Permanent destination = harness.addToBattlefieldAndReturn(player1, new CrabappleCohort());
        source.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);
        destination.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        cast(source, destination);

        assertThat(source.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(destination.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(destination.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The two target creatures must be different")
    void rejectsSameCreatureForBothTargets() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CrabappleCohort());
        prepareSpell();

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @DisplayName("Counters do not move when either target gains shroud before resolution")
    void doesNotMoveCountersWhenEitherTargetGainsShroud(boolean sourceGainsShroud) {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new CrabappleCohort());
        Permanent destination = harness.addToBattlefieldAndReturn(player2, new CrabappleCohort());
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        prepareSpell();
        harness.castInstant(player1, 0, List.of(source.getId(), destination.getId()));
        (sourceGainsShroud ? source : destination).getPersistentGrantedKeywords().add(Keyword.SHROUD);

        harness.passBothPriorities();

        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(destination.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @DisplayName("Counters do not move when either target leaves the battlefield")
    void doesNotMoveCountersWhenEitherTargetLeaves(boolean sourceLeaves) {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new CrabappleCohort());
        Permanent destination = harness.addToBattlefieldAndReturn(player2, new CrabappleCohort());
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        prepareSpell();
        harness.castInstant(player1, 0, List.of(source.getId(), destination.getId()));
        gd.playerBattlefields.get(sourceLeaves ? player1.getId() : player2.getId())
                .remove(sourceLeaves ? source : destination);

        harness.passBothPriorities();

        assertThat(destination.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        if (!sourceLeaves) {
            assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        }
    }

    private void prepareSpell() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new FateTransfer()));
        harness.addMana(player1, ManaColor.BLUE, 2);
    }

    private void cast(Permanent source, Permanent destination) {
        prepareSpell();

        harness.castAndResolveInstant(player1, 0, List.of(source.getId(), destination.getId()));
    }
}
