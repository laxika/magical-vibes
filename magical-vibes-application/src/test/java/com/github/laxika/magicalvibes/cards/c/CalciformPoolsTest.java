package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(CalciformPools.class)
class CalciformPoolsTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping it adds one colorless mana")
    void tappingAddsColorlessMana() {
        harness.addToBattlefield(player1, new CalciformPools());

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("The first ability pays {1} and puts a storage counter on the land")
    void firstAbilityAddsStorageCounter() {
        Permanent pools = harness.addToBattlefieldAndReturn(player1, new CalciformPools());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(pools.getCounterCount(CounterType.STORAGE)).isEqualTo(1);
        assertThat(pools.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The second ability removes storage counters and adds white and blue mana")
    void secondAbilityAddsManaInAnyCombinationOfWhiteAndBlue() {
        Permanent pools = harness.addToBattlefieldAndReturn(player1, new CalciformPools());
        pools.setCounterCount(CounterType.STORAGE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        GameData gameData = harness.getGameData();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "2");
        harness.handleListChoice(player1, "WHITE");
        harness.handleListChoice(player1, "BLUE");

        assertThat(gameData.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gameData.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gameData.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(pools.getCounterCount(CounterType.STORAGE)).isEqualTo(1);
        assertThat(pools.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Stored mana can be withdrawn after tapping the land for colorless mana")
    void withdrawsStorageCountersWhileTapped() {
        Permanent pools = harness.addToBattlefieldAndReturn(player1, new CalciformPools());
        pools.setCounterCount(CounterType.STORAGE, 2);
        harness.tapPermanent(player1, 0);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "2");
        harness.handleListChoice(player1, "BLUE");
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(pools.getCounterCount(CounterType.STORAGE)).isZero();
        assertThat(pools.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Stored mana can be withdrawn repeatedly without tapping the land")
    void withdrawsStorageCountersRepeatedly() {
        Permanent pools = harness.addToBattlefieldAndReturn(player1, new CalciformPools());
        pools.setCounterCount(CounterType.STORAGE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "1");
        harness.handleListChoice(player1, "WHITE");
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "1");
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(pools.getCounterCount(CounterType.STORAGE)).isZero();
        assertThat(pools.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Choosing zero storage counters pays one mana without tapping or adding mana")
    void canChooseZeroStorageCounters() {
        Permanent pools = harness.addToBattlefieldAndReturn(player1, new CalciformPools());
        pools.setCounterCount(CounterType.STORAGE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "0");

        assertThat(pools.getCounterCount(CounterType.STORAGE)).isEqualTo(2);
        assertThat(pools.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
