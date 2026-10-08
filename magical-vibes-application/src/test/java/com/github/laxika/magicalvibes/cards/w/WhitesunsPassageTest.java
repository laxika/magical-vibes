package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WhitesunsPassage.class})
class WhitesunsPassageTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Whitesun's Passage puts it on the stack")
    void castingPutsItOnStack() {
        WhitesunsPassage card = new WhitesunsPassage();
        harness.castFromHand(player1, card, "{1}{W}");

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard()).isSameAs(card);
    }

    @Test
    @DisplayName("Whitesun's Passage gains 5 life for its controller")
    void gains5Life() {
        harness.setLife(player1, 15);
        harness.castFromHand(player1, new WhitesunsPassage(), "{1}{W}");
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Whitesun's Passage goes to graveyard after resolution")
    void goesToGraveyardAfterResolution() {
        harness.castFromHand(player1, new WhitesunsPassage(), "{1}{W}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Whitesun's Passage");
    }

    @Test
    @DisplayName("The second player gains life above 20 without affecting the opponent")
    void secondPlayerGainsLifeAbove20() {
        harness.setLife(player1, 12);
        harness.setLife(player2, 23);
        harness.castFromHand(player2, new WhitesunsPassage(), "{1}{W}");

        harness.assertLife(player1, 12);
        harness.assertLife(player2, 23);

        harness.passBothPriorities();

        harness.assertLife(player1, 12);
        harness.assertLife(player2, 28);
        harness.assertInGraveyard(player2, "Whitesun's Passage");
    }
}
