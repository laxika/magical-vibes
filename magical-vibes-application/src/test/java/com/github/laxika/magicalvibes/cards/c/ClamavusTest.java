package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Clamavus.class, GrizzlyBears.class})
class ClamavusTest extends BaseCardTest {

    @Test
    @DisplayName("Each creature gets +1/+1 for each +1/+1 counter on itself")
    void boostsOwnCreaturesBasedOnTheirCounters() {
        harness.addToBattlefield(player1, new Clamavus());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        bears.getCounters().put(CounterType.PLUS_ONE_PLUS_ONE, 2);
        opponentBears.getCounters().put(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opponentBears)).isEqualTo(3);
    }

    @Test
    @DisplayName("The bonus updates when a creature gains another +1/+1 counter")
    void updatesWithCounterChanges() {
        harness.addToBattlefield(player1, new Clamavus());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);

        bears.getCounters().put(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }
}
