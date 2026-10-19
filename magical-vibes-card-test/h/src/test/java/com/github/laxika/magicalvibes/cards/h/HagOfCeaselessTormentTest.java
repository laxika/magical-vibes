package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HagOfCeaselessTorment.class})
class HagOfCeaselessTormentTest extends BaseCardTest {

    @Test
    void opponentWithNothingToSacrificeOrDiscardLosesLife() {
        addCreatureReady(player1, new HagOfCeaselessTorment());
        harness.setHand(player2, List.of());

        advanceToUpkeep(player1);
        resolveAllTriggers();

        harness.assertLife(player2, 17);
    }
}
