package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.Abrade;
import com.github.laxika.magicalvibes.cards.b.BrazenBlademaster;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GeologicalAppraiser.class, Forest.class, GrizzlyBears.class, HillGiant.class,
        BrazenBlademaster.class, Abrade.class})
class GeologicalAppraiserTest extends BaseCardTest {

    @Test
    @DisplayName("When Geological Appraiser enters after being cast, it discovers 3")
    void discoversThreeWhenCast() {
        GrizzlyBears discovered = new GrizzlyBears();
        Forest land = new Forest();
        HillGiant tooExpensive = new HillGiant();
        castAppraiser(List.of(land, tooExpensive, discovered));

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(discovered);

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).contains(discovered);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(land, tooExpensive);
    }

    @Test
    @DisplayName("Discover 3 can cast the found card without paying its mana cost")
    void castsDiscoveredCardForFree() {
        GrizzlyBears discovered = new GrizzlyBears();
        castAppraiser(List.of(discovered));

        harness.handleCardChosen(player1, 0);

        assertThat(gd.stack).anyMatch(entry -> entry.getCard() == discovered
                && entry.getEntryType() == StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("Entering without being cast does not trigger discover")
    void doesNotDiscoverWhenPutOntoBattlefield() {
        BrazenBlademaster top = new BrazenBlademaster();
        harness.setLibrary(player1, List.of(top));

        harness.enterBattlefieldAndReturn(player1, new GeologicalAppraiser());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
    }

    @Test
    @DisplayName("Discover accepts mana value exactly three and leaves unrevealed cards above the rest")
    void discoversAtBoundaryAndBottomsOnlyExiledCards() {
        Forest skipped = new Forest();
        BrazenBlademaster discovered = new BrazenBlademaster();
        Forest unrevealed = new Forest();
        castAppraiser(List.of(skipped, discovered, unrevealed));

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).contains(discovered);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(unrevealed, skipped);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Discover with no qualifying card returns all skipped cards to the library")
    void returnsAllCardsWhenNoCardQualifies() {
        Forest land = new Forest();
        HillGiant tooExpensive = new HillGiant();
        castAppraiser(List.of(land, tooExpensive));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(land, tooExpensive);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Geological Appraiser");
    }

    @Test
    @DisplayName("Discover with an empty library finishes without a choice")
    void finishesWithEmptyLibrary() {
        castAppraiser(List.of());

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Geological Appraiser");
    }

    @Test
    @DisplayName("Discover still resolves after Geological Appraiser dies in response")
    void discoversAfterSourceLeavesBattlefield() {
        BrazenBlademaster discovered = new BrazenBlademaster();
        harness.setLibrary(player1, List.of(discovered));
        harness.setHand(player1, List.of(new GeologicalAppraiser()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Abrade()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castModalInstant(player2, 0, 0,
                List.of(harness.getPermanentId(player1, "Geological Appraiser")));
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Geological Appraiser");
        harness.assertNotOnBattlefield(player1, "Geological Appraiser");
        resolveAllTriggers();

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).contains(discovered);
        assertThat(gd.stack).isEmpty();
    }

    private void castAppraiser(List<com.github.laxika.magicalvibes.model.Card> library) {
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new GeologicalAppraiser()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }
}
