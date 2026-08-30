package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(Whetwheel.class)
class WhetwheelTest extends BaseCardTest {

    @Test
    void millsXCardsForTwiceXMana() {
        harness.addToBattlefield(player1, new Whetwheel());
        trimDeck(player2, 10);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, 3, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void canBeCastFaceDownAndTurnedFaceUpForMorphCost() {
        harness.setHand(player1, List.of(new Whetwheel()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent whetwheel = findPermanent(player1, "Whetwheel");
        assertThat(whetwheel.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(whetwheel));
        harness.passBothPriorities();

        assertThat(whetwheel.isFaceDown()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    private void trimDeck(com.github.laxika.magicalvibes.model.Player player, int size) {
        List<com.github.laxika.magicalvibes.model.Card> deck = gd.playerDecks.get(player.getId());
        while (deck.size() > size) {
            deck.removeFirst();
        }
    }
}
