package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LuxaRiverShrineTest extends BaseCardTest {

    @Test
    @DisplayName("First ability gains 1 life and adds a brick counter")
    void firstAbilityGainsLifeAndAddsBrickCounter() {
        Permanent shrine = addReadyShrine(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        assertThat(shrine.getCounterCount(CounterType.BRICK)).isEqualTo(1);
    }

    @Test
    @DisplayName("Second ability can't be activated with fewer than three brick counters")
    void secondAbilityRequiresThreeBrickCounters() {
        Permanent shrine = addReadyShrine(player1);
        shrine.setCounterCount(CounterType.BRICK, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("brick counters");
    }

    @Test
    @DisplayName("Second ability gains 2 life with three brick counters")
    void secondAbilityGainsTwoLifeWithThreeBrickCounters() {
        Permanent shrine = addReadyShrine(player1);
        shrine.setCounterCount(CounterType.BRICK, 3);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
    }

    private Permanent addReadyShrine(Player player) {
        Permanent perm = new Permanent(new LuxaRiverShrine());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
