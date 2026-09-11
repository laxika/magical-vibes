package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RavenhillFlock.class, GrizzlyBears.class})
class RavenhillFlockTest extends BaseCardTest {

    @Test
    @DisplayName("Each card drawn puts a +1/+1 counter on Ravenhill Flock")
    void eachDrawAddsCounter() {
        Permanent flock = harness.addToBattlefieldAndReturn(player1, new RavenhillFlock());
        gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());
        gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());

        draw(player1.getId());
        draw(player1.getId());
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(flock.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent drawing a card does not trigger Ravenhill Flock")
    void doesNotTriggerOnOpponentDraw() {
        Permanent flock = harness.addToBattlefieldAndReturn(player1, new RavenhillFlock());
        gd.playerDecks.get(player2.getId()).add(new GrizzlyBears());

        draw(player2.getId());

        assertThat(flock.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void draw(UUID playerId) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, playerId));
    }
}
