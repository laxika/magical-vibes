package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.ArchiveTrap;
import com.github.laxika.magicalvibes.cards.w.WelkinTern;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TrapmakersSnare.class, ArchiveTrap.class, WelkinTern.class})
class TrapmakersSnareTest extends BaseCardTest {

    @Test
    @DisplayName("Offers only Trap cards from the library")
    void offersOnlyTrapCards() {
        setUpLibrary();
        castAndResolve();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting(Card::getName)
                .containsExactly("Archive Trap", "Archive Trap");
        assertThat(search.params().reveals()).isTrue();
    }

    @Test
    @DisplayName("Choosing a Trap puts it into hand")
    void choosingTrapPutsItIntoHand() {
        setUpLibrary();
        castAndResolve();

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        harness.assertInHand(player1, "Archive Trap");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("May fail to find even when a Trap is present")
    void mayFailToFind() {
        ArchiveTrap trap = new ArchiveTrap();
        WelkinTern creature = new WelkinTern();
        harness.setLibrary(player1, List.of(trap, creature));
        castAndResolve();

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(trap, creature);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Trapmaker's Snare");
    }

    @Test
    @DisplayName("Resolves without finding a card when the library has no Traps")
    void noTrapsInLibrary() {
        WelkinTern creature = new WelkinTern();
        harness.setLibrary(player1, List.of(creature));
        castAndResolve();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Trapmaker's Snare");
    }

    @Test
    @DisplayName("Resolves normally with an empty library")
    void emptyLibrary() {
        harness.setLibrary(player1, List.of());
        castAndResolve();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Trapmaker's Snare");
    }

    private void setUpLibrary() {
        harness.setLibrary(player1, List.of(new ArchiveTrap(), new WelkinTern(), new ArchiveTrap()));
    }

    private void castAndResolve() {
        harness.setHand(player1, List.of(new TrapmakersSnare()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }
}
