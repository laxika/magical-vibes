package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HagOfDeathsLegion.class})
class HagOfDeathsLegionTest extends BaseCardTest {

    @Test
    void upkeepCreatesSkeletonToken() {
        addCreatureReady(player1, new HagOfDeathsLegion());

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Skeleton")).isEqualTo(1);
    }
}
