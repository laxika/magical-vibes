package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InterdisciplinaryMascot.class, GrizzlyBears.class})
class InterdisciplinaryMascotTest extends BaseCardTest {

    @Test
    @DisplayName("When it enters, puts one of the top four cards into its controller's hand")
    void selectsOneOfTopFourCards() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        Card third = new GrizzlyBears();
        Card fourth = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second, third, fourth));

        harness.castFromHand(player1, new InterdisciplinaryMascot(), "{6}{U}{U}");
        resolveAllTriggers();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.allCards()).containsExactly(first, second, third, fourth);
        assertThat(choice.maxCount()).isEqualTo(1);

        harness.handleMultipleCardsChosen(player1, List.of(third.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(third);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(first, second, fourth);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("When fewer than four cards remain, it still puts one card into its controller's hand")
    void handlesShortLibrary() {
        Card only = new GrizzlyBears();
        harness.setLibrary(player1, List.of(only));

        harness.castFromHand(player1, new InterdisciplinaryMascot(), "{6}{U}{U}");
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(only);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
