package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SandstoneNeedleTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped with two depletion counters")
    void entersTappedWithTwoDepletionCounters() {
        harness.setHand(player1, List.of(new SandstoneNeedle()));
        harness.playLand(player1, 0);

        Permanent needle = findPermanent(player1, "Sandstone Needle");
        assertThat(needle.isTapped()).isTrue();
        assertThat(needle.getCounterCount(CounterType.DEPLETION)).isEqualTo(2);
    }

    @Test
    @DisplayName("Removing a depletion counter adds two red mana and keeps the land")
    void removesCounterAndAddsTwoRedMana() {
        Permanent needle = addNeedle(2);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(redMana()).isEqualTo(2);
        assertThat(needle.getCounterCount(CounterType.DEPLETION)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Sandstone Needle");
    }

    @Test
    @DisplayName("Removing the last depletion counter adds mana and sacrifices the land")
    void removesLastCounterAndSacrifices() {
        Permanent needle = addNeedle(1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(redMana()).isEqualTo(2);
        assertThat(needle.getCounterCount(CounterType.DEPLETION)).isZero();
        harness.assertNotOnBattlefield(player1, "Sandstone Needle");
        harness.assertInGraveyard(player1, "Sandstone Needle");
    }

    @Test
    @DisplayName("Cannot activate without a depletion counter")
    void cannotActivateWithoutDepletionCounter() {
        addNeedle(0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addNeedle(int counters) {
        Permanent needle = harness.addToBattlefieldAndReturn(player1, new SandstoneNeedle());
        needle.setSummoningSick(false);
        needle.setCounterCount(CounterType.DEPLETION, counters);
        return needle;
    }

    private int redMana() {
        return gd.playerManaPools.get(player1.getId()).get(ManaColor.RED);
    }
}
