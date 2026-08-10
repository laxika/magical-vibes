package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ViridianJoinerTest extends BaseCardTest {

    @Test
    @DisplayName("Tap ability produces green mana equal to its power")
    void tapProducesGreenManaEqualToPower() {
        harness.addToBattlefield(player1, new ViridianJoiner());

        Permanent joiner = gd.playerBattlefields.get(player1.getId()).getFirst();
        joiner.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tap ability uses the creature's current power")
    void tapUsesCurrentPower() {
        harness.addToBattlefield(player1, new ViridianJoiner());

        Permanent joiner = gd.playerBattlefields.get(player1.getId()).getFirst();
        joiner.setSummoningSick(false);
        joiner.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(3);
    }
}
