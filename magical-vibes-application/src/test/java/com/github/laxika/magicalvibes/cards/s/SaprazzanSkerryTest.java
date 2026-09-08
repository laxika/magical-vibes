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

class SaprazzanSkerryTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped with two depletion counters")
    void entersTappedWithTwoDepletionCounters() {
        harness.setHand(player1, List.of(new SaprazzanSkerry()));

        harness.playLand(player1, 0);

        Permanent skerry = findPermanent(player1, "Saprazzan Skerry");
        assertThat(skerry.isTapped()).isTrue();
        assertThat(skerry.getCounterCount(CounterType.DEPLETION)).isEqualTo(2);
    }

    @Test
    @DisplayName("Activating Saprazzan Skerry removes one counter and adds two blue mana")
    void activationRemovesOneCounterAndAddsTwoBlueMana() {
        Permanent skerry = addReadySkerry(2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(2);
        assertThat(skerry.getCounterCount(CounterType.DEPLETION)).isEqualTo(1);
        assertThat(skerry.isTapped()).isTrue();
        assertThat(findPermanent(player1, "Saprazzan Skerry")).isSameAs(skerry);
    }

    @Test
    @DisplayName("Activating Saprazzan Skerry with its last counter adds mana and sacrifices it")
    void lastCounterSacrificesSkerryAfterAddingMana() {
        Permanent skerry = addReadySkerry(1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(2);
        assertThat(skerry.getCounterCount(CounterType.DEPLETION)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(skerry);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(skerry.getCard());
    }

    @Test
    @DisplayName("Saprazzan Skerry cannot be activated without a depletion counter")
    void cannotActivateWithoutDepletionCounter() {
        addReadySkerry(0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadySkerry(int counters) {
        Permanent skerry = harness.addToBattlefieldAndReturn(player1, new SaprazzanSkerry());
        skerry.setSummoningSick(false);
        skerry.setCounterCount(CounterType.DEPLETION, counters);
        return skerry;
    }
}
