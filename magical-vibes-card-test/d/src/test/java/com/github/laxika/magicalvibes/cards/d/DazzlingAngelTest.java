package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DazzlingAngel.class})
class DazzlingAngelTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 1 life when another creature you control enters")
    void gainsLifeOnAnotherAllyCreatureEnter() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new DazzlingAngel());
        harness.castFromHand(player1, new DazzlingAngel(), "{2}{W}");

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Does not gain life when an opponent's creature enters")
    void noLifeOnOpponentCreatureEnter() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new DazzlingAngel());
        harness.enterBattlefieldAndReturn(player2, new DazzlingAngel());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Does not trigger for itself entering")
    void noLifeOnSelfEntering() {
        harness.setLife(player1, 20);
        harness.castFromHand(player1, new DazzlingAngel(), "{2}{W}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Each existing Angel triggers independently and gains life only on resolution")
    void multipleAngelsTriggerIndependently() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new DazzlingAngel());
        harness.addToBattlefield(player1, new DazzlingAngel());
        harness.castFromHand(player1, new DazzlingAngel(), "{2}{W}");

        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);

        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
