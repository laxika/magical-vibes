package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MageRingNetwork.class})
class MageRingNetworkTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping for mana adds one colorless")
    void tapForColorless() {
        Permanent network = addNetwork(0);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(colorlessMana()).isEqualTo(1);
        assertThat(network.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The second ability puts a storage counter on the land")
    void storesACounter() {
        Permanent network = addNetwork(0);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(network.getCounterCount(CounterType.STORAGE)).isEqualTo(1);
        assertThat(network.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Removing all storage counters adds that much colorless mana")
    void removingAllCountersAddsColorless() {
        Permanent network = addNetwork(3);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.handleListChoice(player1, "3");

        assertThat(colorlessMana()).isEqualTo(3);
        assertThat(network.getCounterCount(CounterType.STORAGE)).isZero();
        assertThat(network.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Removing fewer counters than present keeps the rest")
    void removingSomeCountersKeepsTheRest() {
        Permanent network = addNetwork(3);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.handleListChoice(player1, "1");

        assertThat(colorlessMana()).isEqualTo(1);
        assertThat(network.getCounterCount(CounterType.STORAGE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Removing zero counters produces no mana but still taps the land")
    void removingZeroCountersProducesNoMana() {
        Permanent network = addNetwork(3);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.handleListChoice(player1, "0");

        assertThat(colorlessMana()).isZero();
        assertThat(network.getCounterCount(CounterType.STORAGE)).isEqualTo(3);
        assertThat(network.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activating the removal ability with no counters produces no mana and no choice")
    void noCountersNoChoice() {
        Permanent network = addNetwork(0);

        harness.activateAbility(player1, 0, 2, null, null);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(colorlessMana()).isZero();
        assertThat(network.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Storing a counter pays mana immediately but uses the stack")
    void storingUsesStackAndPaysMana() {
        Permanent network = addNetwork(2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(colorlessMana()).isZero();
        assertThat(network.isTapped()).isTrue();
        assertThat(network.getCounterCount(CounterType.STORAGE)).isEqualTo(2);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(network.getCounterCount(CounterType.STORAGE)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Storing a counter requires one mana")
    void storingRequiresMana() {
        Permanent network = addNetwork(0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(network.isTapped()).isFalse();
        assertThat(network.getCounterCount(CounterType.STORAGE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A newly entered land can remove storage counters for mana without using the stack")
    void newlyEnteredLandProducesStoredManaImmediately() {
        Permanent network = addNetwork(2);
        network.setSummoningSick(true);
        network.setCounterCount(CounterType.CHARGE, 1);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.handleListChoice(player1, "2");

        assertThat(colorlessMana()).isEqualTo(2);
        assertThat(network.getCounterCount(CounterType.STORAGE)).isZero();
        assertThat(network.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(network.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addNetwork(int counters) {
        Permanent network = harness.addToBattlefieldAndReturn(player1, new MageRingNetwork());
        network.setSummoningSick(false);
        if (counters > 0) {
            network.setCounterCount(CounterType.STORAGE, counters);
        }
        return network;
    }

    private int colorlessMana() {
        return gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS);
    }
}
