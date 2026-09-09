package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(VenerableMonk.class)
class VenerableMonkTest extends BaseCardTest {

    @Test
    @DisplayName("Its controller gains 2 life when it enters")
    void controllerGainsTwoLifeWhenItEnters() {
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castFromHand(player1, new VenerableMonk(), "{2}{W}");
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore);
    }
}
