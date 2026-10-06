package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.j.JasmineDragonTeaShop;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SharedRoots.class, Forest.class, JasmineDragonTeaShop.class})
class SharedRootsTest extends BaseCardTest {

    @Test
    @DisplayName("Searches for a basic land and puts it onto the battlefield tapped")
    void searchesForBasicLandOntoBattlefieldTapped() {
        harness.setHand(player1, List.of(new SharedRoots()));
        harness.setLibrary(player1, List.of(new Forest(), new SharedRoots()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards())
                .singleElement()
                .satisfies(card -> assertThat(card.hasType(CardType.LAND)).isTrue());
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);

        harness.handleCardChosen(player1, 0);

        Permanent forest = findPermanent(player1, "Forest");
        assertThat(forest.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Does not prompt when the library contains no basic land")
    void noBasicLandNoPrompt() {
        harness.setHand(player1, List.of(new SharedRoots()));
        harness.setLibrary(player1, List.of(new SharedRoots()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("Cannot search for a nonbasic land")
    void excludesNonbasicLands() {
        Forest forest = new Forest();
        JasmineDragonTeaShop nonbasic = new JasmineDragonTeaShop();
        harness.setHand(player1, List.of(new SharedRoots()));
        harness.setLibrary(player1, List.of(nonbasic, forest));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(forest);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonbasic);
        harness.assertNotOnBattlefield(player1, "Jasmine Dragon Tea Shop");
        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
    }

    @Test
    @DisplayName("May find no land even when a basic land is available")
    void mayFailToFindAvailableBasicLand() {
        Forest forest = new Forest();
        harness.setHand(player1, List.of(new SharedRoots()));
        harness.setLibrary(player1, List.of(forest));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        harness.assertNotOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("Finds only one basic land and leaves the other in the library")
    void findsOnlyOneBasicLand() {
        Forest first = new Forest();
        Forest second = new Forest();
        harness.setHand(player1, List.of(new SharedRoots()));
        harness.setLibrary(player1, List.of(first, second));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .singleElement().satisfies(permanent -> {
                    assertThat(permanent.getCard()).isSameAs(first);
                    assertThat(permanent.isTapped()).isTrue();
                });
        harness.assertNotOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("Resolves without a choice when the library is empty")
    void emptyLibraryResolvesWithoutChoice() {
        harness.setHand(player1, List.of(new SharedRoots()));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertNotOnBattlefield(player1, "Forest");
    }
}
