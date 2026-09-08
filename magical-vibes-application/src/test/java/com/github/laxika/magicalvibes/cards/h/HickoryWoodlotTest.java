package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HickoryWoodlotTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped with two depletion counters")
    void entersTappedWithTwoDepletionCounters() {
        harness.setHand(player1, List.of(new HickoryWoodlot()));
        harness.playLand(player1, 0);

        Permanent woodlot = findPermanent(player1, "Hickory Woodlot");
        assertThat(woodlot.isTapped()).isTrue();
        assertThat(woodlot.getCounterCount(CounterType.DEPLETION)).isEqualTo(2);
    }

    @Test
    @DisplayName("Removing a depletion counter adds two green mana and keeps the land")
    void removesCounterAndAddsTwoGreenMana() {
        Permanent woodlot = addWoodlot(2);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(greenMana()).isEqualTo(2);
        assertThat(woodlot.getCounterCount(CounterType.DEPLETION)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Hickory Woodlot");
    }

    @Test
    @DisplayName("Removing the last depletion counter adds mana and sacrifices the land")
    void removesLastCounterAndSacrifices() {
        Permanent woodlot = addWoodlot(1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(greenMana()).isEqualTo(2);
        assertThat(woodlot.getCounterCount(CounterType.DEPLETION)).isZero();
        harness.assertNotOnBattlefield(player1, "Hickory Woodlot");
        harness.assertInGraveyard(player1, "Hickory Woodlot");
    }

    @Test
    @DisplayName("Cannot activate without a depletion counter")
    void cannotActivateWithoutDepletionCounter() {
        addWoodlot(0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addWoodlot(int counters) {
        Permanent woodlot = harness.addToBattlefieldAndReturn(player1, new HickoryWoodlot());
        woodlot.setSummoningSick(false);
        woodlot.setCounterCount(CounterType.DEPLETION, counters);
        return woodlot;
    }

    private int greenMana() {
        return gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN);
    }
}
