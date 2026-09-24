package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(PeatBog.class)
class PeatBogTest extends BaseCardTest {

    @Test
    @DisplayName("Peat Bog enters tapped with two depletion counters")
    void entersTappedWithTwoDepletionCounters() {
        harness.setHand(player1, List.of(new PeatBog()));

        harness.playLand(player1, 0);

        Permanent bog = findPermanent(player1, "Peat Bog");
        assertThat(bog.isTapped()).isTrue();
        assertThat(bog.getCounterCount(CounterType.DEPLETION)).isEqualTo(2);
    }

    @Test
    @DisplayName("Activating Peat Bog removes one counter and adds two black mana")
    void activationRemovesOneCounterAndAddsTwoBlackMana() {
        Permanent bog = addReadyBog(2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(2);
        assertThat(bog.getCounterCount(CounterType.DEPLETION)).isEqualTo(1);
        assertThat(bog.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Peat Bog");
    }

    @Test
    @DisplayName("Activating Peat Bog with its last counter adds mana and sacrifices it")
    void lastCounterSacrificesBogAfterAddingMana() {
        Permanent bog = addReadyBog(1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(2);
        assertThat(bog.getCounterCount(CounterType.DEPLETION)).isZero();
        harness.assertNotOnBattlefield(player1, "Peat Bog");
        harness.assertInGraveyard(player1, "Peat Bog");
    }

    @Test
    @DisplayName("Peat Bog cannot be activated without a depletion counter")
    void cannotActivateWithoutDepletionCounter() {
        addReadyBog(0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Peat Bog cannot be activated while it is tapped")
    void cannotActivateWhenTapped() {
        addReadyBog(2);

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    private Permanent addReadyBog(int counters) {
        Permanent bog = harness.addToBattlefieldAndReturn(player1, new PeatBog());
        bog.setSummoningSick(false);
        bog.setCounterCount(CounterType.DEPLETION, counters);
        return bog;
    }
}
