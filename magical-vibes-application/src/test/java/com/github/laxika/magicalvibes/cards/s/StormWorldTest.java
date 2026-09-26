package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DurkwoodBoars;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

@CardUsed({StormWorld.class, DurkwoodBoars.class})
class StormWorldTest extends BaseCardTest {

    private List<Card> cards(int count) {
        return Stream.generate(DurkwoodBoars::new).limit(count).map(Card.class::cast).toList();
    }

    @Test
    @DisplayName("Deals four damage when the active player's hand is empty")
    void dealsFourDamageWithEmptyHand() {
        harness.addToBattlefield(player1, new StormWorld());
        harness.setHand(player1, List.of());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 16);
    }

    @Test
    @DisplayName("Deals damage to the active player equal to four minus their hand size")
    void damagesActivePlayerByHandDeficit() {
        harness.addToBattlefield(player1, new StormWorld());
        harness.setHand(player1, cards(2));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Deals damage during each player's own upkeep")
    void damagesEachActivePlayer() {
        harness.addToBattlefield(player1, new StormWorld());
        harness.setHand(player2, cards(3));

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Deals no damage when the active player has four cards in hand")
    void dealsNoDamageWithFourCards() {
        harness.addToBattlefield(player1, new StormWorld());
        harness.setHand(player1, cards(4));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Deals no damage when the active player has more than four cards in hand")
    void dealsNoDamageWithMoreThanFourCards() {
        harness.addToBattlefield(player1, new StormWorld());
        harness.setHand(player1, cards(5));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Recomputes damage from the active player's hand size at resolution")
    void amountRecomputedAtResolution() {
        harness.addToBattlefield(player1, new StormWorld());
        harness.setHand(player1, cards(2));

        advanceToUpkeep(player1);
        harness.setHand(player1, cards(1));
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
    }
}
