package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MindSculptTest extends BaseCardTest {

    @Test
    @DisplayName("Target opponent mills seven cards")
    void millsSevenCards() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new MindSculpt()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        List<Card> deck = gd.playerDecks.get(player2.getId());
        while (deck.size() > 10) {
            deck.removeFirst();
        }

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(7);
    }

    @Test
    @DisplayName("Mills only the remaining cards when the library is smaller than seven")
    void millsOnlyRemainingWhenLibrarySmall() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new MindSculpt()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        List<Card> deck = gd.playerDecks.get(player2.getId());
        while (deck.size() > 4) {
            deck.removeFirst();
        }

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetController() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new MindSculpt()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
