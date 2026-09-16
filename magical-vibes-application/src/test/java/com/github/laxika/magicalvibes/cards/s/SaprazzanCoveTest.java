package com.github.laxika.magicalvibes.cards.s;

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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(SaprazzanCove.class)
class SaprazzanCoveTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new SaprazzanCove()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Saprazzan Cove").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping it puts a storage counter on it")
    void tappingItPutsStorageCounterOnIt() {
        Permanent cove = harness.addToBattlefieldAndReturn(player1, new SaprazzanCove());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(cove.getCounterCount(CounterType.STORAGE)).isEqualTo(1);
        assertThat(cove.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Removing storage counters adds that much blue mana")
    void removingStorageCountersAddsBlueMana() {
        Permanent cove = harness.addToBattlefieldAndReturn(player1, new SaprazzanCove());
        cove.setCounterCount(CounterType.STORAGE, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "2");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(2);
        assertThat(cove.getCounterCount(CounterType.STORAGE)).isEqualTo(1);
        assertThat(cove.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Removing zero storage counters produces no mana but still taps the land")
    void removingZeroStorageCountersProducesNoMana() {
        Permanent cove = harness.addToBattlefieldAndReturn(player1, new SaprazzanCove());
        cove.setCounterCount(CounterType.STORAGE, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "0");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(cove.getCounterCount(CounterType.STORAGE)).isEqualTo(3);
        assertThat(cove.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activating with no storage counters produces no mana and no choice")
    void activatingWithNoStorageCountersProducesNoManaAndNoChoice() {
        Permanent cove = harness.addToBattlefieldAndReturn(player1, new SaprazzanCove());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(cove.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Removing storage counters leaves other counter types untouched")
    void removingStorageCountersLeavesOtherCounterTypesUntouched() {
        Permanent cove = harness.addToBattlefieldAndReturn(player1, new SaprazzanCove());
        cove.setCounterCount(CounterType.STORAGE, 2);
        cove.setCounterCount(CounterType.CHARGE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "1");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(cove.getCounterCount(CounterType.STORAGE)).isEqualTo(1);
        assertThat(cove.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The blue-mana ability cannot be activated while the land is tapped")
    void cannotActivateManaAbilityWhileTapped() {
        Permanent cove = harness.addToBattlefieldAndReturn(player1, new SaprazzanCove());
        cove.setCounterCount(CounterType.STORAGE, 1);
        cove.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }
}
