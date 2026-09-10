package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AlertShuInfantry;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.SouthernElephant;
import com.github.laxika.magicalvibes.cards.v.VolunteerMilitia;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ImperialRecruiter.class, AlertShuInfantry.class, VolunteerMilitia.class,
        SouthernElephant.class, Plains.class})
class ImperialRecruiterTest extends BaseCardTest {

    @Test
    @DisplayName("ETB offers only creature cards with power 2 or less")
    void etbOffersOnlyLowPowerCreatures() {
        setupAndCast();
        List<Card> library = setupLibrary();

        resolveEtb();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .containsExactlyInAnyOrder(library.get(0), library.get(1));
        assertThat(search.params().reveals()).isTrue();
        assertThat(search.params().canFailToFind()).isTrue();
    }

    @Test
    @DisplayName("Choosing a creature puts it into hand and shuffles library")
    void choosingCreaturePutsItIntoHand() {
        setupAndCast();
        setupLibrary();

        resolveEtb();

        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        List<Card> offered = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards();
        Card chosenCard = offered.getFirst();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card == chosenCard);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    @Test
    @DisplayName("Player can fail to find, leaving hand empty and library shuffled")
    void canFailToFind() {
        setupAndCast();
        setupLibrary();

        resolveEtb();

        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    @Test
    @DisplayName("ETB with no matching creatures finds nothing")
    void noMatchingCreatures() {
        setupAndCast();

        harness.setLibrary(player1, List.of(new SouthernElephant(), new Plains()));

        resolveEtb();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    private void setupAndCast() {
        harness.castFromHand(player1, new ImperialRecruiter(), "{2}{R}");
    }

    private List<Card> setupLibrary() {
        List<Card> library = List.of(
                new AlertShuInfantry(), new VolunteerMilitia(), new SouthernElephant(), new Plains());
        harness.setLibrary(player1, library);
        return library;
    }

    private void resolveEtb() {
        resolveAllTriggers();
    }
}
