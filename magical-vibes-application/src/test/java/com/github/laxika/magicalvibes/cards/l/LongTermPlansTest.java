package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LongTermPlans.class, GiantGrowth.class, GrizzlyBears.class, Plains.class})
class LongTermPlansTest extends BaseCardTest {

    @Test
    @DisplayName("Searches for any card and puts it third from the top")
    void searchesForCardAndPutsItThirdFromTop() {
        Card firstCard = new GrizzlyBears();
        Card secondCard = new Plains();
        Card chosenCard = new GiantGrowth();
        setLibrary(List.of(firstCard, secondCard, chosenCard));
        harness.setHand(player1, List.of(new LongTermPlans()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().reveals()).isFalse();
        assertThat(search.params().canFailToFind()).isFalse();

        int chosenIndex = search.params().cards().indexOf(chosenCard);
        assertThat(chosenIndex).isGreaterThanOrEqualTo(0);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(chosenIndex));

        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library.get(2).getId()).isEqualTo(chosenCard.getId());
        assertThat(library).extracting(Card::getId)
                .containsExactlyInAnyOrder(firstCard.getId(), secondCard.getId(), chosenCard.getId());
    }

    @Test
    @DisplayName("Puts the searched card on the bottom when fewer than three cards remain")
    void putsCardOnBottomWhenLibraryIsShort() {
        Card firstCard = new GrizzlyBears();
        Card chosenCard = new GiantGrowth();
        setLibrary(List.of(firstCard, chosenCard));
        harness.setHand(player1, List.of(new LongTermPlans()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        int chosenIndex = search.params().cards().indexOf(chosenCard);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(chosenIndex));

        assertThat(gd.playerDecks.get(player1.getId()).get(1).getId()).isEqualTo(chosenCard.getId());
    }

    private void setLibrary(List<Card> cards) {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(cards);
    }
}
