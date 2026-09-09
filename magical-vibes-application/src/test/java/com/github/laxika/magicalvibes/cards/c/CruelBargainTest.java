package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.p.PlatinumAngel;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CruelBargain.class, PlatinumAngel.class})
class CruelBargainTest extends BaseCardTest {

    private void cast() {
        harness.castFromHand(player1, new CruelBargain(), "{B}{B}{B}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Resolving draws four cards")
    void drawsFourCards() {
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        cast();

        // Spell left hand, then four cards drawn.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 4);
    }

    @Test
    @DisplayName("From even life total, lose exactly half")
    void losesHalfFromEvenLife() {
        harness.setLife(player1, 20);

        cast();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("From odd life total, lose half rounded up")
    void losesHalfRoundedUpFromOddLife() {
        harness.setLife(player1, 21);

        cast();

        // Half of 21 is 10.5, rounded up to 11 lost -> 10 remaining.
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("Uses the controller's life total when it resolves")
    void usesLifeTotalAtResolution() {
        harness.setLife(player1, 19);
        harness.castFromHand(player1, new CruelBargain(), "{B}{B}{B}");
        harness.setLife(player1, 21);

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(10);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Does not gain life when half of a negative life total is negative")
    void doesNotGainLifeFromNegativeLifeTotal() {
        harness.addToBattlefield(player1, new PlatinumAngel());
        harness.setLife(player1, -3);
        harness.runStateBasedActions();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);

        harness.castFromHand(player1, new CruelBargain(), "{B}{B}{B}");
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(-3);
    }
}
