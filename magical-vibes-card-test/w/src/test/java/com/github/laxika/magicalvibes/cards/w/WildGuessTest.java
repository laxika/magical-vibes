package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WildGuess.class, Forest.class})
class WildGuessTest extends BaseCardTest {

    @Test
    @DisplayName("Discards a card as a cost, then draws two")
    void discardsThenDrawsTwo() {
        harness.setHand(player1, List.of(new WildGuess(), new Forest()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorceryWithDiscard(player1, 0, 1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Forest");
        // Started with 2 cards, cast one, discarded one (net 0), then drew two.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Cannot be cast with no other card to discard")
    void cannotCastWithoutCardToDiscard() {
        harness.setHand(player1, List.of(new WildGuess()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castSorceryWithDiscard(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("A cast missing the discard selection is rejected before any cost is paid")
    void rejectedCastLeavesManaAndHandUntouched() {
        harness.setHand(player1, List.of(new WildGuess(), new Forest()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must discard");

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(2);
    }

    @Test
    @DisplayName("Discard is paid before resolution and drawing waits for resolution")
    void paysDiscardBeforeDrawing() {
        Forest discarded = new Forest();
        Forest firstDraw = new Forest();
        Forest secondDraw = new Forest();
        harness.setHand(player1, List.of(new WildGuess(), discarded));
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorceryWithDiscard(player1, 0, 1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        harness.assertInGraveyard(player1, "Wild Guess");
    }

    @Test
    @DisplayName("Can discard another Wild Guess, including a card before the spell in hand")
    void canDiscardNonlandBeforeSpellInHand() {
        WildGuess discarded = new WildGuess();
        WildGuess spell = new WildGuess();
        harness.setHand(player1, List.of(discarded, spell));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorceryWithDiscard(player1, 1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded, spell);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }
}
