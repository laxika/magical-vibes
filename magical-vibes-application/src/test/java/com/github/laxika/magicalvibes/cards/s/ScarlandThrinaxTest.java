package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ScarlandThrinaxTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another creature puts a +1/+1 counter on Scarland Thrinax")
    void sacrificeCreatureAddsCounter() {
        Permanent thrinax = addCreatureReady(player1, new ScarlandThrinax());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(bears.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(thrinax.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Repeated sacrifices accumulate +1/+1 counters")
    void multipleActivationsAccumulateCounters() {
        Permanent thrinax = addCreatureReady(player1, new ScarlandThrinax());
        Permanent bears1 = addCreatureReady(player1, new GrizzlyBears());
        Permanent bears2 = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, bears1.getId());
        harness.passBothPriorities();
        assertThat(thrinax.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, bears2.getId());
        harness.passBothPriorities();
        assertThat(thrinax.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Scarland Thrinax may sacrifice itself (cost is 'a creature', not 'another')")
    void canSacrificeItself() {
        Permanent thrinax = addCreatureReady(player1, new ScarlandThrinax());

        // Only creature on the battlefield — the cost auto-selects Scarland itself.
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(thrinax.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Scarland Thrinax"));
    }
}
