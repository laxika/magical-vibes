package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(SacredNectar.class)
class SacredNectarTest extends BaseCardTest {

    @Test
    @DisplayName("Sacred Nectar gains 4 life for its controller")
    void gains4Life() {
        harness.setLife(player1, 20);
        harness.castFromHand(player1, new SacredNectar(), "{1}{W}");
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerLifeTotals.get(player1.getId())).isEqualTo(24);
    }
}
