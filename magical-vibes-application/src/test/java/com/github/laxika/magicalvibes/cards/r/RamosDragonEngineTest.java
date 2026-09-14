package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.l.LightningHelix;
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

@CardUsed({RamosDragonEngine.class, LightningHelix.class})
class RamosDragonEngineTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a multicolored spell puts one counter on Ramos for each color")
    void castsSpellAndCountsItsColors() {
        Permanent ramos = harness.addToBattlefieldAndReturn(player1, new RamosDragonEngine());
        harness.setHand(player1, List.of(new LightningHelix()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(ramos.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Removing five counters adds two mana of each color")
    void removesFiveCountersForTenMana() {
        Permanent ramos = harness.addToBattlefieldAndReturn(player1, new RamosDragonEngine());
        ramos.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 5);

        harness.activateAbility(player1, 0, null, null);

        assertThat(ramos.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ramos's mana ability can be activated only once each turn")
    void manaAbilityIsLimitedToOncePerTurn() {
        Permanent ramos = harness.addToBattlefieldAndReturn(player1, new RamosDragonEngine());
        ramos.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 10);

        harness.activateAbility(player1, 0, null, null);
        ramos.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(ramos.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }
}
