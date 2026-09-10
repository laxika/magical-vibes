package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.y.YavimayaWurm;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IronMaiden.class, YavimayaWurm.class})
class IronMaidenTest extends BaseCardTest {

    private List<Card> cards(int count) {
        return Stream.generate(YavimayaWurm::new).limit(count).map(Card.class::cast).toList();
    }

    @Test
    @DisplayName("Deals damage equal to the opponent's hand size minus four")
    void dealsScalingDamage() {
        harness.addToBattlefield(player1, new IronMaiden());
        harness.setHand(player2, cards(6));
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("Deals no damage when the opponent has exactly four cards in hand")
    void noDamageWithFourCards() {
        harness.addToBattlefield(player1, new IronMaiden());
        harness.setHand(player2, cards(4));
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Deals no damage when the opponent has fewer than four cards in hand")
    void noDamageWithFewerThanFourCards() {
        harness.addToBattlefield(player1, new IronMaiden());
        harness.setHand(player2, cards(1));
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Does not trigger during the controller's own upkeep")
    void doesNotTriggerDuringOwnUpkeep() {
        harness.addToBattlefield(player1, new IronMaiden());
        harness.setHand(player1, cards(7));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Recomputes the damage from the hand size at resolution")
    void amountRecomputedAtResolution() {
        harness.addToBattlefield(player1, new IronMaiden());
        harness.setHand(player2, cards(6));
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.setHand(player2, cards(8));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 4);
    }
}
