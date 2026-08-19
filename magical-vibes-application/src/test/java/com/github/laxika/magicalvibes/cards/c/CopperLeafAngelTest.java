package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CopperLeafAngelTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing X lands puts X +1/+1 counters on Copper-Leaf Angel")
    void sacrificesLandsAndPutsCounters() {
        Permanent angel = addCreatureReady(player1, new CopperLeafAngel());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Mountain());

        harness.activateAbility(player1, 0, 2, null);
        harness.passBothPriorities();

        assertThat(angel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(angel);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Cannot sacrifice more lands than are controlled")
    void cannotSacrificeMoreLandsThanControlled() {
        addCreatureReady(player1, new CopperLeafAngel());
        harness.addToBattlefield(player1, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
