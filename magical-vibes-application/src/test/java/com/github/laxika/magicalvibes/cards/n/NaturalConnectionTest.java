package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NaturalConnection.class, Forest.class, Plains.class, GrizzlyBears.class})
class NaturalConnectionTest extends BaseCardTest {

    @Test
    @DisplayName("Searches for a basic land and offers it to enter the battlefield tapped")
    void searchesForBasicLandToBattlefieldTapped() {
        Forest forest = new Forest();
        Plains plains = new Plains();
        castWithLibrary(List.of(forest, new GrizzlyBears(), plains));

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(forest, plains);
        assertThat(search.params().cards())
                .allMatch(card -> card.hasType(CardType.LAND)
                        && card.getSupertypes().contains(CardSupertype.BASIC));
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
        assertThat(search.params().canFailToFind()).isTrue();
    }

    @Test
    @DisplayName("Puts the chosen basic land onto the battlefield tapped")
    void chosenBasicLandEntersTapped() {
        Forest forest = new Forest();
        castWithLibrary(List.of(forest, new GrizzlyBears()));

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, search.params().cards().indexOf(forest));

        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Natural Connection");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castWithLibrary(List<Card> library) {
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new NaturalConnection()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }
}
