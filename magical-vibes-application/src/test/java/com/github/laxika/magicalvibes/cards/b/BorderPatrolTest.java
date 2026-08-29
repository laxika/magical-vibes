package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(BorderPatrol.class)
class BorderPatrolTest extends BaseCardTest {

    @Test
    void vigilanceKeepsBorderPatrolUntappedAfterAttacking() {
        Permanent patrol = addCreatureReady(player1, new BorderPatrol());

        declareAttackers(player1, List.of(0));

        assertThat(patrol.isTapped()).isFalse();
    }
}
