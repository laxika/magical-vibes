package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(RushwoodGrove.class)
class RushwoodGroveTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new RushwoodGrove()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Rushwood Grove").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping it puts a storage counter on it")
    void tappingItPutsStorageCounterOnIt() {
        Permanent grove = harness.addToBattlefieldAndReturn(player1, new RushwoodGrove());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(grove.getCounterCount(CounterType.STORAGE)).isEqualTo(1);
        assertThat(grove.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Removing storage counters adds that much green mana")
    void removingStorageCountersAddsGreenMana() {
        Permanent grove = harness.addToBattlefieldAndReturn(player1, new RushwoodGrove());
        grove.setCounterCount(CounterType.STORAGE, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "2");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(grove.getCounterCount(CounterType.STORAGE)).isEqualTo(1);
        assertThat(grove.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Removing all storage counters adds one green mana for each counter")
    void removingAllStorageCountersAddsGreenMana() {
        Permanent grove = addGroveWithCounters(3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "3");

        assertThat(greenMana()).isEqualTo(3);
        assertThat(grove.getCounterCount(CounterType.STORAGE)).isZero();
        assertThat(grove.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Removing zero storage counters produces no mana but still taps the land")
    void removingZeroStorageCountersProducesNoMana() {
        Permanent grove = addGroveWithCounters(3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "0");

        assertThat(greenMana()).isZero();
        assertThat(grove.getCounterCount(CounterType.STORAGE)).isEqualTo(3);
        assertThat(grove.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activating with no storage counters produces no mana and no choice")
    void activatingWithNoStorageCountersProducesNoMana() {
        Permanent grove = addGroveWithCounters(0);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(greenMana()).isZero();
        assertThat(grove.isTapped()).isTrue();
    }

    private Permanent addGroveWithCounters(int counters) {
        Permanent grove = harness.addToBattlefieldAndReturn(player1, new RushwoodGrove());
        grove.setCounterCount(CounterType.STORAGE, counters);
        return grove;
    }

    private int greenMana() {
        return gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN);
    }
}
