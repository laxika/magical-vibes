package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Fireball;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DarkTutelage.class, Forest.class, RuneclawBear.class, Fireball.class})
class DarkTutelageTest extends BaseCardTest {

    @Test
    @DisplayName("Reveals top card, puts it into hand, and loses life equal to mana value")
    void revealsAndPutsIntoHandAndLosesLife() {
        harness.addToBattlefield(player1, new DarkTutelage());
        harness.setHand(player1, List.of());
        Card topCard = new RuneclawBear(); // MV 2
        gd.playerDecks.get(player1.getId()).addFirst(topCard);
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(topCard.getId()));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Revealing a land (mana value 0) causes no life loss")
    void revealingLandCausesNoLifeLoss() {
        harness.addToBattlefield(player1, new DarkTutelage());
        harness.setHand(player1, List.of());
        Card topCard = new Forest(); // MV 0
        gd.playerDecks.get(player1.getId()).addFirst(topCard);
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(topCard.getId()));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Does not trigger during opponent's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new DarkTutelage());
        harness.setHand(player1, List.of());
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.setLife(player1, 20);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Card is removed from the top of the library")
    void cardIsRemovedFromLibrary() {
        harness.addToBattlefield(player1, new DarkTutelage());
        harness.setHand(player1, List.of());
        Card topCard = new RuneclawBear();
        gd.playerDecks.get(player1.getId()).addFirst(topCard);
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
        assertThat(gd.playerDecks.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(topCard.getId()));
    }

    @Test
    @DisplayName("Does nothing when library is empty")
    void doesNothingWhenLibraryEmpty() {
        harness.addToBattlefield(player1, new DarkTutelage());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of());
        harness.setLife(player1, 20);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("X in the revealed card's mana cost contributes zero to life loss")
    void revealingXSpellUsesManaValueOutsideStack() {
        harness.addToBattlefield(player1, new DarkTutelage());
        Card topCard = new Fireball();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(topCard));
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(topCard);
        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Each copy reveals the current top card when its trigger resolves")
    void multipleCopiesRevealSuccessiveCards() {
        harness.addToBattlefield(player1, new DarkTutelage());
        harness.addToBattlefield(player1, new DarkTutelage());
        Card firstCard = new RuneclawBear();
        Card secondCard = new Fireball();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(firstCard, secondCard));
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstCard);
        harness.assertLife(player1, 18);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstCard, secondCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 17);
    }

    @Test
    @DisplayName("A triggered ability still resolves after Dark Tutelage leaves the battlefield")
    void triggerResolvesWithoutSource() {
        harness.addToBattlefield(player1, new DarkTutelage());
        Card topCard = new RuneclawBear();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(topCard));
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(topCard);
        harness.assertLife(player1, 18);
    }
}
