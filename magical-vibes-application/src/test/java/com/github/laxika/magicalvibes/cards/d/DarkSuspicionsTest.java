package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(DarkSuspicions.class)
class DarkSuspicionsTest extends BaseCardTest {

    private List<Card> cards(int count) {
        return Stream.generate(DarkSuspicions::new).limit(count).map(Card.class::cast).toList();
    }

    @Test
    @DisplayName("Opponent loses the hand-size difference during their upkeep")
    void losesDifferenceBetweenHandSizes() {
        harness.addToBattlefield(player1, new DarkSuspicions());
        harness.setHand(player1, cards(2));
        harness.setHand(player2, cards(5));
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 3);
    }

    @Test
    @DisplayName("Does not cause life loss when the opponent does not have more cards")
    void floorsLifeLossAtZero() {
        harness.addToBattlefield(player1, new DarkSuspicions());
        harness.setHand(player1, cards(4));
        harness.setHand(player2, cards(2));
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Does not trigger during the controller's upkeep")
    void doesNotTriggerDuringOwnUpkeep() {
        harness.addToBattlefield(player1, new DarkSuspicions());
        harness.setHand(player1, cards(1));
        harness.setHand(player2, cards(5));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Recomputes the difference from hand sizes at resolution")
    void amountRecomputedAtResolution() {
        harness.addToBattlefield(player1, new DarkSuspicions());
        harness.setHand(player1, cards(2));
        harness.setHand(player2, cards(5));
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        gd.playerHands.get(player1.getId()).add(new DarkSuspicions());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("Triggers for an opponent's upkeep even when controlled by the other player")
    void triggersForOpponentsUpkeepUnderEitherControl() {
        harness.addToBattlefield(player2, new DarkSuspicions());
        harness.setHand(player1, cards(5));
        harness.setHand(player2, cards(2));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 3);
    }
}
