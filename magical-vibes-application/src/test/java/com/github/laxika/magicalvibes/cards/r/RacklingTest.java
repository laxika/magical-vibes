package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.i.IvoryMask;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(Rackling.class)
class RacklingTest extends BaseCardTest {

    private List<Card> racklings(int count) {
        return Stream.generate(Rackling::new).limit(count).map(Card.class::cast).toList();
    }

    @Test
    @DisplayName("Deals damage equal to three minus the opponent's hand size")
    void dealsScalingDamage() {
        harness.addToBattlefield(player1, new Rackling());
        harness.setHand(player2, racklings(1));
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("Deals three damage when the opponent has an empty hand")
    void emptyHandDealsMaximumDamage() {
        harness.addToBattlefield(player1, new Rackling());
        harness.setHand(player2, List.of());
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 3);
    }

    @Test
    @DisplayName("Deals no damage when the opponent has three or more cards in hand")
    void noDamageWithThreeOrMoreCards() {
        harness.addToBattlefield(player1, new Rackling());
        harness.setHand(player2, racklings(3));
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Does not trigger during the controller's own upkeep")
    void doesNotTriggerDuringOwnUpkeep() {
        harness.addToBattlefield(player1, new Rackling());
        harness.setHand(player1, List.of());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Recomputes damage from the opponent's hand size at resolution")
    void amountRecomputedAtResolution() {
        harness.addToBattlefield(player1, new Rackling());
        harness.setHand(player2, racklings(1));
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.setHand(player2, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 3);
    }

    @Test
    @CardUsed(IvoryMask.class)
    @DisplayName("Shroud does not stop Rackling's non-targeting trigger")
    void shroudDoesNotStopNonTargetingTrigger() {
        harness.addToBattlefield(player1, new Rackling());
        harness.addToBattlefield(player2, new IvoryMask());
        harness.setHand(player2, List.of());
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 3);
    }
}
