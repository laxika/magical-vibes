package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class SkullcageTest extends BaseCardTest {

    private List<Card> handOf(int size) {
        return IntStream.range(0, size).mapToObj(i -> (Card) new GrizzlyBears()).toList();
    }

    private void assertNoDamageWithHandSize(int handSize) {
        harness.addToBattlefield(player1, new Skullcage());
        harness.setHand(player2, handOf(handSize));
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Deals 2 damage with two or fewer cards in the opponent's hand")
    void dealsDamageWithSmallHand() {
        harness.addToBattlefield(player1, new Skullcage());
        harness.setHand(player2, handOf(2));
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("Deals no damage with exactly three cards in the opponent's hand")
    void dealsNoDamageWithThreeCards() {
        assertNoDamageWithHandSize(3);
    }

    @Test
    @DisplayName("Deals no damage with exactly four cards in the opponent's hand")
    void dealsNoDamageWithFourCards() {
        assertNoDamageWithHandSize(4);
    }

    @Test
    @DisplayName("Deals 2 damage with five or more cards in the opponent's hand")
    void dealsDamageWithLargeHand() {
        harness.addToBattlefield(player1, new Skullcage());
        harness.setHand(player2, handOf(5));
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("The unless condition is checked when the trigger resolves")
    void checksHandSizeAtResolution() {
        harness.addToBattlefield(player1, new Skullcage());
        harness.setHand(player2, handOf(3));
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        gd.playerHands.get(player2.getId()).remove(0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("Does not trigger during the controller's upkeep")
    void doesNotTriggerDuringOwnUpkeep() {
        harness.addToBattlefield(player1, new Skullcage());
        harness.setHand(player1, handOf(5));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }
}
