package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.e.ElvishMystic;
import com.github.laxika.magicalvibes.cards.e.EncroachingWastes;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LayOfTheLand.class, ElvishMystic.class, EncroachingWastes.class, Forest.class, Island.class, Plains.class})
class LayOfTheLandTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving offers only basic lands, destination hand")
    void resolvesOffersBasicLandsToHand() {
        castLayOfTheLand();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).hasSize(3);
        assertThat(search.params().cards())
                .allMatch(c -> c.hasType(CardType.LAND) && c.getSupertypes().contains(CardSupertype.BASIC));
        assertThat(search.params().reveals()).isTrue();
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.HAND);
        assertThat(search.params().canFailToFind()).isTrue();
    }

    @Test
    @DisplayName("Chosen basic land goes to hand")
    void chosenBasicLandGoesToHand() {
        castLayOfTheLand();

        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Plains");
        harness.assertInGraveyard(player1, "Lay of the Land");
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactlyInAnyOrder("Forest", "Island", "Elvish Mystic", "Encroaching Wastes");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Player may fail to find and the library is shuffled")
    void mayFailToFind() {
        castLayOfTheLand();

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        List<String> libraryBefore = gd.playerDecks.get(player1.getId()).stream()
                .map(card -> card.getName())
                .toList();

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactlyInAnyOrderElementsOf(libraryBefore);
        assertThat(gameLogContains("chooses not to take a card. Library is shuffled.")).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("No basic land resolves without a choice and shuffles the library")
    void noBasicLandDoesNotPrompt() {
        castLayOfTheLand(new ElvishMystic(), new EncroachingWastes());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Lay of the Land");
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactlyInAnyOrder("Elvish Mystic", "Encroaching Wastes");
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    private void castLayOfTheLand() {
        castLayOfTheLand(new Plains(), new Forest(), new Island(), new ElvishMystic(), new EncroachingWastes());
    }

    private void castLayOfTheLand(Card... libraryCards) {
        harness.setLibrary(player1, List.of(libraryCards));
        harness.castFromHand(player1, new LayOfTheLand(), "{G}");

        harness.passBothPriorities();
    }
}
