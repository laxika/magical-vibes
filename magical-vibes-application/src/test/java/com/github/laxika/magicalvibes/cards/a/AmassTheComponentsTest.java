package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AmassTheComponentsTest extends BaseCardTest {

    private List<Card> fiveCards() {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            cards.add(i % 2 == 0 ? new GrizzlyBears() : new Shock());
        }
        return cards;
    }

    private void castAmassTheComponents(List<Card> library) {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(library);

        harness.setHand(player1, List.of(new AmassTheComponents()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Draws three cards, then asks for one hand card to bottom")
    void drawsThreeThenPrompts() {
        List<Card> library = fiveCards();
        castAmassTheComponents(library);

        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactly(library.get(0), library.get(1), library.get(2));
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOfSatisfying(PendingInteraction.PutCardsFromHandOnLibraryCardChoice.class, choice -> {
                    assertThat(choice.playerId()).isEqualTo(player1.getId());
                    assertThat(choice.maxCount()).isEqualTo(1);
                });
    }

    @Test
    @DisplayName("Chosen card goes on the bottom of the library with no top/bottom prompt")
    void putsChosenCardOnBottom() {
        List<Card> library = fiveCards();
        castAmassTheComponents(library);

        Card drawn0 = library.get(0);
        Card drawn1 = library.get(1);
        Card drawn2 = library.get(2);

        harness.handleMultipleCardsChosen(player1, List.of(drawn1.getId()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn0, drawn2);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(library.get(3), library.get(4), drawn1);
    }
}
