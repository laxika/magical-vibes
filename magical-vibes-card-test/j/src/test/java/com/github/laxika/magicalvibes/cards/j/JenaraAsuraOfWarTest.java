package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JenaraAsuraOfWar.class})
class JenaraAsuraOfWarTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {1}{W} puts a +1/+1 counter on Jenara")
    void activationAddsCounter() {
        Permanent jenara = addJenara(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(jenara.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Multiple activations accumulate +1/+1 counters")
    void multipleActivationsAccumulate() {
        Permanent jenara = addJenara(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(jenara.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutMana() {
        addJenara(player1);

        org.assertj.core.api.Assertions.assertThatThrownBy(
                () -> harness.activateAbility(player1, 0, null, null)
        ).isInstanceOf(IllegalStateException.class);
    }

    private Permanent addJenara(Player player) {
        return addCreatureReady(player, new JenaraAsuraOfWar());
    }

    @Test
    @DisplayName("Jenara can activate while tapped and summoning sick")
    void activatesWhileTappedAndSummoningSick() {
        Permanent jenara = harness.addToBattlefieldAndReturn(player1, new JenaraAsuraOfWar());
        jenara.setSummoningSick(true);
        jenara.tap();
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(jenara.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(jenara.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Two colorless mana cannot pay the required white mana")
    void cannotActivateWithoutWhiteMana() {
        Permanent jenara = addJenara(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        org.assertj.core.api.Assertions.assertThatThrownBy(
                () -> harness.activateAbility(player1, 0, null, null)
        ).isInstanceOf(IllegalStateException.class);

        assertThat(jenara.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The counter is placed on resolution rather than activation")
    void counterWaitsForResolution() {
        Permanent jenara = addJenara(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(jenara.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(jenara.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
