package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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

    private void addReadyMonaLisa() {
        harness.addToBattlefield(player1, new MonaLisaScienceGeek());
        gd.playerBattlefields.get(player1.getId()).getFirst().setSummoningSick(false);
    }
}
