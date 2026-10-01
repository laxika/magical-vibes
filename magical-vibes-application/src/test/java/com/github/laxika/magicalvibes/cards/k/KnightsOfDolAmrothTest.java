package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KnightsOfDolAmroth.class, GrizzlyBears.class})
class KnightsOfDolAmrothTest extends BaseCardTest {

    @Test
    @DisplayName("Drawing the second card each turn puts a +1/+1 counter on Knights of Dol Amroth")
    void secondDrawAddsCounterOnlyOnce() {
        Permanent knights = harness.addToBattlefieldAndReturn(player1, new KnightsOfDolAmroth());
        harness.setLibrary(player1, java.util.List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        draw(player1.getId());
        assertThat(knights.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        draw(player1.getId());
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
        assertThat(knights.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        draw(player1.getId());
        assertThat(gd.stack).isEmpty();
        assertThat(knights.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void draw(UUID playerId) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, playerId));
    }
}
