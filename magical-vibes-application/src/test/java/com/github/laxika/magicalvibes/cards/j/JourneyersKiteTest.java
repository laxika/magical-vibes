package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.a.AkkiAvalanchers;
import com.github.laxika.magicalvibes.cards.f.ForbiddenOrchard;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JourneyersKite.class, Forest.class, Plains.class, ForbiddenOrchard.class,
        AkkiAvalanchers.class})
class JourneyersKiteTest extends BaseCardTest {

    @Test
    @DisplayName("Activating offers only basic lands and keeps the Kite on the battlefield")
    void offersOnlyBasicLands() {
        activateSearch();

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Journeyer's Kite");
        assertThat(findPermanent(player1, "Journeyer's Kite").isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().reveals()).isTrue();
        assertThat(search.params().canFailToFind()).isTrue();
        assertThat(search.params().cards())
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Forest", "Plains");
        assertThat(search.params().destination())
                .isEqualTo(LibrarySearchDestination.HAND);
    }

    @Test
    @DisplayName("Exactly one chosen basic land goes to hand")
    void chosenLandGoesToHand() {
        activateSearch();

        harness.passBothPriorities();

        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        harness.assertInHand(player1, "Forest");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Player may fail to find")
    void canFailToFind() {
        activateSearch();

        harness.passBothPriorities();
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("No basic land in the library produces no search choice")
    void noBasicLandFound() {
        activateSearch(List.of(new ForbiddenOrchard(), new AkkiAvalanchers()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Forbidden Orchard", "Akki Avalanchers");
    }

    private void activateSearch() {
        activateSearch(List.of(new Forest(), new Plains(), new ForbiddenOrchard(), new AkkiAvalanchers()));
    }

    private void activateSearch(List<Card> deck) {
        harness.addToBattlefield(player1, new JourneyersKite());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setLibrary(player1, deck);
        harness.activateAbility(player1, 0, null, null);
    }
}
