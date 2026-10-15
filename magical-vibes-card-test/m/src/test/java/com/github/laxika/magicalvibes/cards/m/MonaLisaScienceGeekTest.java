package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(MonaLisaScienceGeek.class)
class MonaLisaScienceGeekTest extends BaseCardTest {

    @Test
    void tapAbilityAddsChosenColorEqualToPower() {
        addReadyMonaLisa();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    void tapAbilityUsesCurrentPower() {
        addReadyMonaLisa();
        var monaLisa = gd.playerBattlefields.get(player1.getId()).getFirst();
        monaLisa.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, ManaColor.RED.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(3);
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    void producesAllManaInOneChosenColorWithoutUsingStack(ManaColor chosenColor) {
        addReadyMonaLisa();
        var monaLisa = gd.playerBattlefields.get(player1.getId()).getFirst();
        monaLisa.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.stack).isEmpty();
        harness.handleListChoice(player1, chosenColor.name());

        for (ManaColor color : ManaColor.values()) {
            assertThat(gd.playerManaPools.get(player1.getId()).get(color))
                    .isEqualTo(color == chosenColor ? 3 : 0);
        }
        assertThat(monaLisa.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2})
    void nonpositivePowerProducesNoManaButStillPaysTapCost(int counters) {
        addReadyMonaLisa();
        var monaLisa = gd.playerBattlefields.get(player1.getId()).getFirst();
        monaLisa.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, counters);

        harness.activateAbility(player1, 0, 0, null, null);

        for (ManaColor color : ManaColor.values()) {
            assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isZero();
        }
        assertThat(monaLisa.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        var monaLisa = harness.addToBattlefieldAndReturn(player1, new MonaLisaScienceGeek());
        monaLisa.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(monaLisa.isTapped()).isFalse();
    }

    @Test
    void cannotActivateAgainWhileTapped() {
        addReadyMonaLisa();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, ManaColor.GREEN.name());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    private void addReadyMonaLisa() {
        harness.addToBattlefieldAndReturn(player1, new MonaLisaScienceGeek()).setSummoningSick(false);
    }
}
