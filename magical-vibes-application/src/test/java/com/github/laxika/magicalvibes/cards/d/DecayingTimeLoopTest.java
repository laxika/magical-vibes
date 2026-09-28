package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DecayingTimeLoop.class, Mountain.class})
class DecayingTimeLoopTest extends BaseCardTest {

    @Test
    @DisplayName("Discards the hand and draws the same number of cards")
    void discardsHandThenDrawsThatMany() {
        Card spell = new DecayingTimeLoop();
        Card discardedOne = new Mountain();
        Card discardedTwo = new Mountain();
        Card drawnOne = new Mountain();
        Card drawnTwo = new Mountain();
        setDeck(player1, List.of(drawnOne, drawnTwo));
        harness.setHand(player1, List.of(spell, discardedOne, discardedTwo));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(drawnOne, drawnTwo);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(spell, discardedOne, discardedTwo);
    }

    @Test
    @DisplayName("Retrace discards a land and returns the spell to the graveyard")
    void retraceDiscardsLandAndReturnsToGraveyard() {
        Card spell = new DecayingTimeLoop();
        Card land = new Mountain();
        harness.setGraveyard(player1, List.of(spell));
        harness.setHand(player1, List.of(land));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castRetrace(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(land, spell);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(spell);
    }

    @Test
    @DisplayName("Retrace requires a land card to be discarded")
    void retraceRequiresLandDiscard() {
        harness.setGraveyard(player1, List.of(new DecayingTimeLoop()));
        harness.setHand(player1, List.of(new DecayingTimeLoop()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castRetrace(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    private void setDeck(Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
