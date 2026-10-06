package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SaltcrustedSteppe.class})
class SaltcrustedSteppeTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping it adds one colorless mana")
    void tappingAddsColorlessMana() {
        harness.addToBattlefield(player1, new SaltcrustedSteppe());

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("The first ability pays {1} and puts a storage counter on the land")
    void firstAbilityAddsStorageCounter() {
        Permanent steppe = harness.addToBattlefieldAndReturn(player1, new SaltcrustedSteppe());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(steppe.getCounterCount(CounterType.STORAGE)).isEqualTo(1);
        assertThat(steppe.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The second ability removes storage counters and adds green and white mana")
    void secondAbilityAddsManaInAnyCombinationOfGreenAndWhite() {
        Permanent steppe = harness.addToBattlefieldAndReturn(player1, new SaltcrustedSteppe());
        steppe.setCounterCount(CounterType.STORAGE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "2");
        harness.handleListChoice(player1, "GREEN");
        harness.handleListChoice(player1, "WHITE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(steppe.getCounterCount(CounterType.STORAGE)).isEqualTo(1);
        assertThat(steppe.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The counter-removal mana ability can use the land's own colorless mana while tapped")
    void removesCountersWhileTapped() {
        Permanent steppe = harness.addToBattlefieldAndReturn(player1, new SaltcrustedSteppe());
        steppe.setCounterCount(CounterType.STORAGE, 2);
        harness.tapPermanent(player1, 0);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "2");
        harness.handleListChoice(player1, "GREEN");
        harness.handleListChoice(player1, "GREEN");

        assertThat(steppe.isTapped()).isTrue();
        assertThat(steppe.getCounterCount(CounterType.STORAGE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The counter-removal mana ability can be activated repeatedly without untapping")
    void removesCountersRepeatedly() {
        Permanent steppe = harness.addToBattlefieldAndReturn(player1, new SaltcrustedSteppe());
        steppe.setCounterCount(CounterType.STORAGE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "1");
        harness.handleListChoice(player1, "WHITE");
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "2");
        harness.handleListChoice(player1, "WHITE");
        harness.handleListChoice(player1, "WHITE");

        assertThat(steppe.isTapped()).isFalse();
        assertThat(steppe.getCounterCount(CounterType.STORAGE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Choosing zero counters still pays one mana and leaves the land untapped")
    void removesZeroCounters() {
        Permanent steppe = harness.addToBattlefieldAndReturn(player1, new SaltcrustedSteppe());
        steppe.setCounterCount(CounterType.STORAGE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "0");

        assertThat(steppe.isTapped()).isFalse();
        assertThat(steppe.getCounterCount(CounterType.STORAGE)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
