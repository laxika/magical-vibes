package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.w.Werebear;
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

@CardUsed({DeepReconnaissance.class, Plains.class, Forest.class, Island.class, Werebear.class})
class DeepReconnaissanceTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving offers only basic lands to enter the battlefield tapped")
    void resolvesBasicLandSearch() {
        harness.castFromHand(player1, new DeepReconnaissance(), "{2}{G}");
        setupLibrary();

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).hasSize(3);
        assertThat(search.params().cards())
                .allMatch(c -> c.hasType(CardType.LAND) && c.getSupertypes().contains(CardSupertype.BASIC));
        assertThat(search.params().destination())
                .isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
        assertThat(search.params().reveals()).isFalse();
        assertThat(search.params().canFailToFind()).isTrue();
    }

    @Test
    @DisplayName("Choosing a basic land puts it onto the battlefield tapped")
    void chosenBasicLandEntersTappedWhenCastNormally() {
        harness.castFromHand(player1, new DeepReconnaissance(), "{2}{G}");
        setupLibrary();

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().hasType(CardType.LAND) && p.isTapped());
        harness.assertInGraveyard(player1, "Deep Reconnaissance");
    }

    @Test
    @DisplayName("Flashback searches for a basic land and exiles the card after resolution")
    void flashbackSearchesAndExiles() {
        harness.setGraveyard(player1, List.of(new DeepReconnaissance()));
        harness.addMana(player1, ManaColor.GREEN, 5);
        setupLibrary();

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().hasType(CardType.LAND) && p.isTapped());
        harness.assertNotInGraveyard(player1, "Deep Reconnaissance");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Deep Reconnaissance"));
    }

    @Test
    @DisplayName("With no basic land in the library, the search resolves without putting a card onto the battlefield")
    void noBasicLandCanBeFound() {
        Werebear nonBasicCard = new Werebear();
        harness.castFromHand(player1, new DeepReconnaissance(), "{2}{G}");
        harness.setLibrary(player1, List.of(nonBasicCard));

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonBasicCard);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().equals(nonBasicCard));
        harness.assertInGraveyard(player1, "Deep Reconnaissance");
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new Plains(), new Forest(), new Island(), new Werebear()));
    }
}
