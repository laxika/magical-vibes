package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Brainstone.class, Island.class})
class BrainstoneTest extends BaseCardTest {

    private List<Card> fiveCards() {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            cards.add(new Island());
        }
        return cards;
    }

    private void activateBrainstone(List<Card> library) {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, library);
        harness.addToBattlefield(player1, new Brainstone());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Sacrifices itself, draws three cards, then asks which two to put on top")
    void sacrificesDrawsThreeThenPrompts() {
        List<Card> library = fiveCards();
        activateBrainstone(library);

        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactly(library.get(0), library.get(1), library.get(2));
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PutCardsFromHandOnLibraryCardChoice.class);
        harness.assertInGraveyard(player1, "Brainstone");
    }

    @Test
    @DisplayName("Puts the chosen cards on top in the chosen order")
    void putsChosenCardsOnTop() {
        List<Card> library = fiveCards();
        activateBrainstone(library);

        Card drawn0 = library.get(0);
        Card drawn1 = library.get(1);
        Card drawn2 = library.get(2);
        harness.handleMultipleCardsChosen(player1, List.of(drawn0.getId(), drawn1.getId()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn2);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(drawn0, drawn1, library.get(3), library.get(4));
    }
}
