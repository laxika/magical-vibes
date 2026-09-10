package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LakeshoreApothecary.class, GrizzlyBears.class})
class LakeshoreApothecaryTest extends BaseCardTest {

    @Test
    @DisplayName("Drawing the second card each turn puts a +1/+1 counter on Lakeshore Apothecary")
    void secondDrawAddsCounterOnlyOnce() {
        Permanent apothecary = harness.addToBattlefieldAndReturn(player1, new LakeshoreApothecary());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        drawCard();
        assertThat(apothecary.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        drawCard();
        assertThat(gd.stack).hasSize(1);
        drawCard();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(apothecary.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void drawCard() {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
    }
}
