package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PeemaTrailblazer.class, Forest.class})
class PeemaTrailblazerTest extends BaseCardTest {

    @Test
    void gainsEnergyEqualToCombatDamageDealtToPlayer() {
        Permanent trailblazer = addCreatureReady(player1, new PeemaTrailblazer());
        trailblazer.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(3);
    }

    @Test
    void paysEnergyPutsCountersOnItselfAndDrawsGreatestControlledPower() {
        Permanent trailblazer = addCreatureReady(player1, new PeemaTrailblazer());
        harness.setLibrary(player1, List.of(
                new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));
        gd.playerEnergyCounters.put(player1.getId(), 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(trailblazer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(5);
    }

    @Test
    void cannotActivateWithoutSixEnergyCounters() {
        addCreatureReady(player1, new PeemaTrailblazer());
        gd.playerEnergyCounters.put(player1.getId(), 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("six energy counters");
    }
}
