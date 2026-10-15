package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(MosscoatGoriak.class)
class MosscoatGoriakTest extends BaseCardTest {

    @Test
    void vigilanceKeepsMosscoatGoriakUntappedAfterAttacking() {
        Permanent goriak = addCreatureReady(player1, new MosscoatGoriak());

        declareAttackers(player1, List.of(0));

        assertThat(goriak.isTapped()).isFalse();
    }
}
