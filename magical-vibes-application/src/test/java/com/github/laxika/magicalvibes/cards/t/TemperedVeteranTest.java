package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AlpineWatchdog;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TemperedVeteran.class, AlpineWatchdog.class})
class TemperedVeteranTest extends BaseCardTest {

    @Test
    void firstAbilityAddsCounterToCreatureThatAlreadyHasOne() {
        addCreatureReady(player1, new TemperedVeteran());
        Permanent target = addCreatureReady(player1, new AlpineWatchdog());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void firstAbilityCannotTargetCreatureWithoutCounter() {
        addCreatureReady(player1, new TemperedVeteran());
        Permanent target = addCreatureReady(player1, new AlpineWatchdog());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void secondAbilityAddsCounterToAnyCreature() {
        addCreatureReady(player1, new TemperedVeteran());
        Permanent target = addCreatureReady(player1, new AlpineWatchdog());
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void bothAbilitiesCanTargetOpponentsCreature(int abilityIndex) {
        Permanent veteran = addCreatureReady(player1, new TemperedVeteran());
        Permanent target = addCreatureReady(player2, new AlpineWatchdog());
        int initialCounters = abilityIndex == 0 ? 1 : 0;
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, initialCounters);
        harness.addMana(player1, ManaColor.WHITE, abilityIndex == 0 ? 1 : 6);

        harness.activateAbility(player1, 0, abilityIndex, null, target.getId());
        assertThat(veteran.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(initialCounters + 1);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void bothAbilitiesCanTargetVeteranItself(int abilityIndex) {
        Permanent veteran = addCreatureReady(player1, new TemperedVeteran());
        int initialCounters = abilityIndex == 0 ? 1 : 0;
        veteran.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, initialCounters);
        harness.addMana(player1, ManaColor.WHITE, abilityIndex == 0 ? 1 : 6);

        harness.activateAbility(player1, 0, abilityIndex, null, veteran.getId());
        harness.passBothPriorities();

        assertThat(veteran.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(initialCounters + 1);
    }

    @Test
    void firstAbilityDoesNothingIfTargetLosesItsLastCounterBeforeResolution() {
        addCreatureReady(player1, new TemperedVeteran());
        Permanent target = addCreatureReady(player2, new AlpineWatchdog());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void summoningSicknessPreventsBothAbilities(int abilityIndex) {
        harness.addToBattlefield(player1, new TemperedVeteran());
        Permanent target = addCreatureReady(player1, new AlpineWatchdog());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.WHITE, 6);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, abilityIndex, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void tappedVeteranCannotActivateEitherAbility(int abilityIndex) {
        Permanent veteran = addCreatureReady(player1, new TemperedVeteran());
        veteran.tap();
        Permanent target = addCreatureReady(player1, new AlpineWatchdog());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.WHITE, 6);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, abilityIndex, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void secondAbilityRequiresTwoWhiteMana() {
        addCreatureReady(player1, new TemperedVeteran());
        Permanent target = addCreatureReady(player1, new AlpineWatchdog());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void secondAbilityAcceptsGenericManaAlongsideTwoWhiteMana() {
        addCreatureReady(player1, new TemperedVeteran());
        Permanent target = addCreatureReady(player1, new AlpineWatchdog());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
