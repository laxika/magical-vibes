package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RemoteFarmTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped with two depletion counters")
    void entersTappedWithTwoDepletionCounters() {
        harness.setHand(player1, List.of(new RemoteFarm()));
        harness.playLand(player1, 0);

        Permanent farm = findPermanent(player1, "Remote Farm");
        assertThat(farm.isTapped()).isTrue();
        assertThat(farm.getCounterCount(CounterType.DEPLETION)).isEqualTo(2);
    }

    @Test
    @DisplayName("Removing a depletion counter adds two white mana and keeps the land")
    void removesCounterAndAddsTwoWhiteMana() {
        Permanent farm = addFarm(2);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(whiteMana()).isEqualTo(2);
        assertThat(farm.getCounterCount(CounterType.DEPLETION)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Remote Farm");
    }

    @Test
    @DisplayName("Removing the last depletion counter adds mana and sacrifices the land")
    void removesLastCounterAndSacrifices() {
        Permanent farm = addFarm(1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(whiteMana()).isEqualTo(2);
        assertThat(farm.getCounterCount(CounterType.DEPLETION)).isZero();
        harness.assertNotOnBattlefield(player1, "Remote Farm");
        harness.assertInGraveyard(player1, "Remote Farm");
    }

    @Test
    @DisplayName("Cannot activate without a depletion counter")
    void cannotActivateWithoutDepletionCounter() {
        addFarm(0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addFarm(int counters) {
        Permanent farm = harness.addToBattlefieldAndReturn(player1, new RemoteFarm());
        farm.setSummoningSick(false);
        farm.setCounterCount(CounterType.DEPLETION, counters);
        return farm;
    }

    private int whiteMana() {
        return gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE);
    }
}
