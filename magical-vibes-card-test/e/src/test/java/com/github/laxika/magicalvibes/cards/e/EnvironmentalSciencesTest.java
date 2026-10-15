package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
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

@CardUsed({EnvironmentalSciences.class, Plains.class, Forest.class, Island.class, GrizzlyBears.class})
class EnvironmentalSciencesTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving offers only basic lands to put into hand")
    void resolvesOffersBasicLandsToHand() {
        castEnvironmentalSciences();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).hasSize(3);
        assertThat(search.params().cards())
                .allMatch(card -> card.hasType(CardType.LAND)
                        && card.getSupertypes().contains(CardSupertype.BASIC));
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.HAND);
        assertThat(search.params().canFailToFind()).isTrue();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Putting a basic land into hand also gives two life")
    void putsBasicLandIntoHandAndGainsLife() {
        castEnvironmentalSciences();

        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Plains");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        harness.assertInGraveyard(player1, "Environmental Sciences");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The found land is revealed and removed only from the caster's library")
    void revealsFoundLandAndLeavesOpponentsLibraryAlone() {
        Plains opponentsLand = new Plains();
        harness.setLibrary(player2, List.of(opponentsLand));
        castEnvironmentalSciences();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().reveals()).isTrue();

        harness.handleCardChosen(player1, 1);

        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactlyInAnyOrder("Plains", "Island", "Grizzly Bears");
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentsLand);
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Environmental Sciences");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Failing to find still gains two life without removing a land")
    void failingToFindStillGainsLife() {
        castEnvironmentalSciences();

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactlyInAnyOrder("Plains", "Forest", "Island", "Grizzly Bears");
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Environmental Sciences");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A library without basic lands still gives two life")
    void noBasicLandsStillGainsLife() {
        harness.setHand(player1, List.of(new EnvironmentalSciences()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Environmental Sciences");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An empty library still gives two life")
    void emptyLibraryStillGainsLife() {
        harness.setHand(player1, List.of(new EnvironmentalSciences()));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Environmental Sciences");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castEnvironmentalSciences() {
        harness.setHand(player1, List.of(new EnvironmentalSciences()));
        harness.setLibrary(player1, List.of(new Plains(), new Forest(), new Island(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, 0);
    }
}
