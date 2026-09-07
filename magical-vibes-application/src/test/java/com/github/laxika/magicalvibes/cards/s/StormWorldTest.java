package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StormWorld.class, GrizzlyBears.class})
class StormWorldTest extends BaseCardTest {

    private List<Card> bears(int count) {
        return Stream.generate(GrizzlyBears::new).limit(count).map(Card.class::cast).toList();
    }

    @Test
    @DisplayName("Deals damage to the active player equal to four minus their hand size")
    void damagesActivePlayerByHandDeficit() {
        harness.addToBattlefield(player1, new StormWorld());
        harness.setHand(player1, bears(2));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("Deals damage during each player's own upkeep")
    void damagesEachActivePlayer() {
        harness.addToBattlefield(player1, new StormWorld());
        harness.setHand(player2, bears(3));
        int player1LifeBefore = gd.playerLifeTotals.get(player1.getId());
        int player2LifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(player1LifeBefore);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(player2LifeBefore - 1);
    }

    @Test
    @DisplayName("Deals no damage when the active player has four or more cards in hand")
    void dealsNoDamageWithFourOrMoreCards() {
        harness.addToBattlefield(player1, new StormWorld());
        harness.setHand(player1, bears(4));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }
}
