package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({ManyPartings.class, Forest.class, GrizzlyBears.class})
class ManyPartingsTest extends BaseCardTest {

    @Test
    @DisplayName("Searches for a basic land and creates a Food token")
    void searchesForBasicLandAndCreatesFood() {
        Forest forest = new Forest();
        castManyPartings(List.of(forest, new GrizzlyBears()));

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(forest);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.HAND);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(forest);
        assertThat(findPermanents(player1, "Food")).hasSize(1);
    }

    @Test
    @DisplayName("Creates a Food token when no basic land is found")
    void createsFoodWithoutBasicLand() {
        castManyPartings(List.of(new GrizzlyBears()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanents(player1, "Food")).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().hasType(CardType.LAND));
    }

    @Test
    @DisplayName("May find no land even when a basic land is available, and still creates Food")
    void canDeclineFindingBasicLand() {
        Forest forest = new Forest();
        castManyPartings(List.of(forest));

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(findPermanents(player1, "Food")).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Creates Food with an empty library")
    void createsFoodWithEmptyLibrary() {
        castManyPartings(List.of());

        assertThat(findPermanents(player1, "Food")).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("New Food can be sacrificed immediately, with life gained only on resolution")
    void foodCanBeUsedImmediately() {
        castManyPartings(List.of());
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(findPermanents(player1, "Food")).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(10);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(13);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.stack).isEmpty();
    }

    private void castManyPartings(List<Card> library) {
        harness.setHand(player1, List.of(new ManyPartings()));
        harness.setLibrary(player1, library);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveSorcery(player1, 0, 0);
    }
}
