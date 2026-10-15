package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
    void millsOnlyCardsRemainingInLibrary() {
        harness.addToBattlefield(player1, new Whetwheel());
        trimDeck(player2, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        harness.activateAbility(player1, 0, 5, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(findPermanent(player1, "Whetwheel").isTapped()).isTrue();
    }

    @Test
    void canTargetItsController() {
        harness.addToBattlefield(player1, new Whetwheel());
        trimDeck(player1, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 2, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
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

    @Test
    void zeroXMillsNothingButStillTaps() {
        harness.addToBattlefield(player1, new Whetwheel());
        int librarySize = gd.playerDecks.get(player2.getId()).size();

        harness.activateAbility(player1, 0, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(librarySize);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(findPermanent(player1, "Whetwheel").isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canMillImmediatelyAfterTurningFaceUp() {
        harness.setHand(player1, List.of(new Whetwheel()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, player2.getId()))
                .isInstanceOf(RuntimeException.class);

        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.turnFaceUp(player1, 0);
        harness.activateAbility(player1, 0, 1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(findPermanent(player1, "Whetwheel").isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    private void trimDeck(com.github.laxika.magicalvibes.model.Player player, int size) {
        List<com.github.laxika.magicalvibes.model.Card> deck = gd.playerDecks.get(player.getId());
        harness.setLibrary(player, List.copyOf(deck.subList(0, size)));
    }
}
