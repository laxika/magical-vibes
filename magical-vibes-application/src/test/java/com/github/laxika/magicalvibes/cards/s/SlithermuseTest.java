package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SlithermuseTest extends BaseCardTest {

    private List<Card> bears(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new GrizzlyBears());
        }
        return cards;
    }

    private void leaveBattlefield(Permanent permanent) {
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, permanent);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // collect LTB trigger onto the stack
        harness.passBothPriorities(); // resolve LTB trigger
    }

    @Test
    @DisplayName("LTB: opponent holds more cards, controller draws the difference")
    void drawsDifferenceWhenOpponentHoldsMore() {
        harness.setHand(player1, bears(1));
        harness.setHand(player2, bears(4));
        harness.setLibrary(player1, bears(5));
        Permanent slithermuse = harness.addToBattlefieldAndReturn(player1, new Slithermuse());

        leaveBattlefield(slithermuse);

        // difference = 4 - 1 = 3 drawn; hand 1 -> 4, library 5 -> 2.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("LTB: opponent holds no more than controller, no cards drawn")
    void drawsNothingWhenOpponentHoldsFewerOrEqual() {
        harness.setHand(player1, bears(3));
        harness.setHand(player2, bears(2));
        harness.setLibrary(player1, bears(5));
        Permanent slithermuse = harness.addToBattlefieldAndReturn(player1, new Slithermuse());

        leaveBattlefield(slithermuse);

        // 2 - 3 is non-positive, so no draw.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
    }

    @Test
    @DisplayName("Evoke: sacrificed on entry, LTB draws the hand-size difference")
    void evokeSacrificesThenDraws() {
        harness.setHand(player1, List.of(new Slithermuse()));
        harness.setHand(player2, bears(3));
        harness.setLibrary(player1, bears(5));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithEvoke(player1, 0, null);
        harness.passBothPriorities(); // resolve creature -> ETB trigger on stack
        harness.passBothPriorities(); // resolve ETB (evoke sacrifice) -> LTB trigger on stack
        harness.passBothPriorities(); // resolve LTB trigger

        // After casting, player1's hand is empty; opponent holds 3 -> draw 3.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Slithermuse"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Slithermuse"));
    }
}
