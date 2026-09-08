package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Wellwisher.class, LlanowarElves.class, GrizzlyBears.class})
class WellwisherTest extends BaseCardTest {

    @Test
    void gainsLifeForEachElfOnBothBattlefieldsAtResolution() {
        Permanent wellwisher = addCreatureReady(player1, new Wellwisher());
        harness.setLife(player1, 10);

        harness.activateAbility(player1, 0, null, null);
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.passBothPriorities();

        assertThat(wellwisher.isTapped()).isTrue();
        harness.assertLife(player1, 13);
    }
}
