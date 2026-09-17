package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AvenFarseer;
import com.github.laxika.magicalvibes.cards.b.BrainFreeze;
import com.github.laxika.magicalvibes.cards.w.WipeClean;
import com.github.laxika.magicalvibes.cards.z.ZealousInquisitor;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LongTermPlans.class, AvenFarseer.class, WipeClean.class, ZealousInquisitor.class,
        BrainFreeze.class})
class LongTermPlansTest extends BaseCardTest {

    @Test
    @DisplayName("Searches for any card and puts it third from the top")
    void searchesForCardAndPutsItThirdFromTop() {
        Card firstCard = new AvenFarseer();
        Card secondCard = new WipeClean();
        Card chosenCard = new ZealousInquisitor();
        Card fourthCard = new BrainFreeze();
        harness.setLibrary(player1, List.of(firstCard, secondCard, chosenCard, fourthCard));
        harness.castFromHand(player1, new LongTermPlans(), "{2}{U}");

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().reveals()).isFalse();
        assertThat(search.params().canFailToFind()).isFalse();

        int chosenIndex = search.params().cards().indexOf(chosenCard);
        assertThat(chosenIndex).isGreaterThanOrEqualTo(0);
        harness.handleCardChosen(player1, chosenIndex);

        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library.get(2).getId()).isEqualTo(chosenCard.getId());
        assertThat(library.get(3).getId()).isNotEqualTo(chosenCard.getId());
        assertThat(library).extracting(Card::getId)
                .containsExactlyInAnyOrder(firstCard.getId(), secondCard.getId(), chosenCard.getId(),
                        fourthCard.getId());
    }

    @Test
    @DisplayName("Puts the searched card on the bottom when fewer than three cards remain")
    void putsCardOnBottomWhenLibraryIsShort() {
        Card firstCard = new AvenFarseer();
        Card chosenCard = new ZealousInquisitor();
        harness.setLibrary(player1, List.of(firstCard, chosenCard));
        harness.castFromHand(player1, new LongTermPlans(), "{2}{U}");

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        int chosenIndex = search.params().cards().indexOf(chosenCard);
        harness.handleCardChosen(player1, chosenIndex);

        assertThat(gd.playerDecks.get(player1.getId()).get(1).getId()).isEqualTo(chosenCard.getId());
    }

    @Test
    @DisplayName("Completes without a choice when the library is empty")
    void completesWithoutChoiceWhenLibraryIsEmpty() {
        harness.setLibrary(player1, List.of());
        harness.castFromHand(player1, new LongTermPlans(), "{2}{U}");

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }
}
