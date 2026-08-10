package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LevelerTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles all cards from its controller's library when it enters")
    void exilesControllerLibraryOnEnter() {
        Card topCard = new GrizzlyBears();
        Card bottomCard = new Shock();
        Card opponentCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard, bottomCard));
        harness.setLibrary(player2, List.of(opponentCard));
        harness.setHand(player1, List.of(new Leveler()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(topCard, bottomCard);
    }
}
