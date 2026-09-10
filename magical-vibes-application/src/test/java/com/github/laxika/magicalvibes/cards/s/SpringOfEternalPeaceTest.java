package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(SpringOfEternalPeace.class)
class SpringOfEternalPeaceTest extends BaseCardTest {

    @Test
    @DisplayName("Spring of Eternal Peace gains 8 life for its controller")
    void gains8Life() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 17);

        harness.castFromHand(player1, new SpringOfEternalPeace(), "{3}{G}{G}");
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerLifeTotals.get(player1.getId())).isEqualTo(28);
        assertThat(harness.getGameData().playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }
}
