package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HagOfSyphonedBreath.class})
class HagOfSyphonedBreathTest extends BaseCardTest {

    @Test
    void upkeepDrainsEachOpponent() {
        addCreatureReady(player1, new HagOfSyphonedBreath());

        advanceToUpkeep(player1);
        resolveAllTriggers();

        harness.assertLife(player2, 18);
        harness.assertLife(player1, 22);
    }
}
