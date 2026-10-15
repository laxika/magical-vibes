package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MoltenSlagheap.class})
class MoltenSlagheapTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping it adds one colorless mana")
    void tappingAddsColorlessMana() {
        harness.addToBattlefield(player1, new MoltenSlagheap());

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("The first ability pays {1} and puts a storage counter on the land")
    void firstAbilityAddsStorageCounter() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new MoltenSlagheap());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(land.getCounterCount(CounterType.STORAGE)).isEqualTo(1);
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The second ability removes storage counters and adds black and red mana")
    void secondAbilityAddsManaInAnyCombinationOfBlackAndRed() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new MoltenSlagheap());
        land.setCounterCount(CounterType.STORAGE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "2");
        harness.handleListChoice(player1, "BLACK");
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(land.getCounterCount(CounterType.STORAGE)).isEqualTo(1);
        assertThat(land.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canProduceStorageManaWhileTapped() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new MoltenSlagheap());
        land.setCounterCount(CounterType.STORAGE, 2);
        harness.tapPermanent(player1, 0);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "2");
        harness.handleListChoice(player1, "RED");
        harness.handleListChoice(player1, "RED");

        assertThat(land.isTapped()).isTrue();
        assertThat(land.getCounterCount(CounterType.STORAGE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canActivateStorageManaAbilityRepeatedlyWithoutUntapping() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new MoltenSlagheap());
        land.setCounterCount(CounterType.STORAGE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        for (int activation = 0; activation < 2; activation++) {
            harness.activateAbility(player1, 0, 1, null, null);
            harness.handleListChoice(player1, "1");
            harness.handleListChoice(player1, "BLACK");
        }

        assertThat(land.isTapped()).isFalse();
        assertThat(land.getCounterCount(CounterType.STORAGE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canChooseZeroStorageCounters() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new MoltenSlagheap());
        land.setCounterCount(CounterType.STORAGE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "0");

        assertThat(land.isTapped()).isFalse();
        assertThat(land.getCounterCount(CounterType.STORAGE)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canActivateStorageManaAbilityWithoutCounters() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new MoltenSlagheap());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(land.isTapped()).isFalse();
        assertThat(land.getCounterCount(CounterType.STORAGE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
