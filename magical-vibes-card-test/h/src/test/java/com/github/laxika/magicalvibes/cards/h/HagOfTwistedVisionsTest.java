package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HagOfTwistedVisions.class})
class HagOfTwistedVisionsTest extends BaseCardTest {

    @Test
    void upkeepScriesTwo() {
        addCreatureReady(player1, new HagOfTwistedVisions());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
    }
}
