package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.e.EbonyOwlNetsuke;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

@CardUsed({IvoryCraneNetsuke.class, EbonyOwlNetsuke.class})
class IvoryCraneNetsukeTest extends BaseCardTest {

    private List<Card> handCards(int count) {
        return Stream.generate(EbonyOwlNetsuke::new).limit(count).map(Card.class::cast).toList();
    }

    @Test
    void gainsFourLifeAtSevenCardsInHand() {
        harness.addToBattlefield(player1, new IvoryCraneNetsuke());
        harness.setHand(player1, handCards(7));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore + 4);
    }

    @Test
    void doesNotTriggerWithFewerThanSevenCardsInHand() {
        harness.addToBattlefield(player1, new IvoryCraneNetsuke());
        harness.setHand(player1, handCards(6));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore);
    }

    @Test
    void doesNotGainLifeIfHandFallsBelowThresholdBeforeResolution() {
        harness.addToBattlefield(player1, new IvoryCraneNetsuke());
        harness.setHand(player1, handCards(7));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        gd.playerHands.get(player1.getId()).remove(0);
        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore);
    }

    @Test
    void onlyTriggersDuringItsControllerUpkeep() {
        harness.addToBattlefield(player1, new IvoryCraneNetsuke());
        harness.setHand(player1, handCards(7));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore);
        harness.assertLife(player2, opponentLifeBefore);
    }
}
