package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class BlackViseTest extends BaseCardTest {

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances to UPKEEP
    }

    private List<Card> bears(int count) {
        return Stream.generate(GrizzlyBears::new).limit(count).map(Card.class::cast).toList();
    }

    @Test
    @DisplayName("Deals (hand size - 4) damage during the opponent's upkeep")
    void dealsScalingDamage() {
        harness.addToBattlefield(player1, new BlackVise());
        harness.setHand(player2, bears(6)); // 6 - 4 = 2 damage
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("Deals no damage when the opponent has exactly four cards in hand")
    void noDamageWithFourCards() {
        harness.addToBattlefield(player1, new BlackVise());
        harness.setHand(player2, bears(4)); // 4 - 4 = 0
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Deals no damage when the opponent has fewer than four cards in hand")
    void noDamageWithFewerCards() {
        harness.addToBattlefield(player1, new BlackVise());
        harness.setHand(player2, bears(1)); // 1 - 4 = -3, clamps to 0
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Does not trigger during the controller's own upkeep")
    void doesNotTriggerDuringOwnUpkeep() {
        harness.addToBattlefield(player1, new BlackVise());
        harness.setHand(player1, bears(7)); // full hand, but controller is unaffected
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Damage is recomputed from the hand size at resolution")
    void amountRecomputedAtResolution() {
        harness.addToBattlefield(player1, new BlackVise());
        harness.setHand(player2, bears(6)); // would be 2 at trigger time
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        // Grow the hand while the trigger is on the stack: 8 - 4 = 4 at resolution.
        gd.playerHands.get(player2.getId()).add(new GrizzlyBears());
        gd.playerHands.get(player2.getId()).add(new GrizzlyBears());
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 4);
    }
}
