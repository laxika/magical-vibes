package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GraniticTitan;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RazakethsRite.class, Plains.class, Swamp.class, GraniticTitan.class})
class RazakethsRiteTest extends BaseCardTest {

    @Test
    @DisplayName("Casting searches library for any card and puts it into hand")
    void searchPutsCardIntoHand() {
        harness.setHand(player1, List.of(new RazakethsRite()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.setLibrary(player1, List.of(new Plains(), new Swamp(), new GraniticTitan()));
        harness.castAndResolveSorcery(player1, 0, 0);

        // Unrestricted search: all library cards offered, cannot fail to find.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).hasSize(3);
        assertThat(search.params().canFailToFind()).isFalse();

        harness.handleCardChosen(player1, 2);

        harness.assertInHand(player1, "Granitic Titan");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Razaketh's Rite");
    }

    @Test
    @DisplayName("Cycling {B} discards Razaketh's Rite and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new RazakethsRite()));
        harness.setLibrary(player1, List.of(new GraniticTitan()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Razaketh's Rite");
        harness.assertInHand(player1, "Granitic Titan");
    }

    @Test
    @DisplayName("Searching an empty library finishes without a choice or a draw")
    void emptyLibrarySearchFinishes() {
        harness.setHand(player1, List.of(new RazakethsRite()));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Razaketh's Rite");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cycling discards as a cost before its draw resolves")
    void cyclingDiscardsBeforeResolution() {
        harness.setHand(player1, List.of(new RazakethsRite()));
        harness.setLibrary(player1, List.of(new Swamp(), new Plains()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Razaketh's Rite");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertInHand(player1, "Swamp");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }
}
