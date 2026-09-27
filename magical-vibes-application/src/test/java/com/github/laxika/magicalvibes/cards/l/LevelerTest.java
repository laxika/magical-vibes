package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Frogmite;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Leveler.class, Frogmite.class})
class LevelerTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles all cards from its controller's library when it enters")
    void exilesControllerLibraryOnEnter() {
        Card topCard = new Frogmite();
        Card bottomCard = new Frogmite();
        Card opponentCard = new Frogmite();
        harness.setLibrary(player1, List.of(topCard, bottomCard));
        harness.setLibrary(player2, List.of(opponentCard));
        harness.castFromHand(player1, new Leveler(), "{5}");

        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(topCard, bottomCard);
    }

    @Test
    @DisplayName("Does not affect the opponent's library when its controller's library is empty")
    void emptyControllerLibraryLeavesOpponentLibraryUntouched() {
        Card opponentCard = new Frogmite();
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of(opponentCard));
        harness.castFromHand(player1, new Leveler(), "{5}");

        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }
}
