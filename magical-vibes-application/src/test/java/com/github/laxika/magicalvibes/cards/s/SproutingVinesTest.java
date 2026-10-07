package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SproutingVines.class, Plains.class, Forest.class, Island.class, GrizzlyBears.class})
class SproutingVinesTest extends BaseCardTest {

    @Test
    @DisplayName("Searches for a basic land and puts it into its controller's hand")
    void searchesForBasicLandToHand() {
        Card plains = new Plains();
        Card forest = new Forest();
        Card island = new Island();
        harness.setLibrary(player1, List.of(plains, forest, island, new GrizzlyBears()));
        castSproutingVines();

        resolveStormAndSpell();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .containsExactlyInAnyOrder(plains, forest, island);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.HAND);
        assertThat(search.params().reveals()).isTrue();
        assertThat(search.params().canFailToFind()).isTrue();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(plains);
        harness.assertInGraveyard(player1, "Sprouting Vines");
    }

    @Test
    @DisplayName("Storm copies also resolve Sprouting Vines' basic-land search")
    void stormCopiesAlsoResolveSearch() {
        Card plains = new Plains();
        Card forest = new Forest();
        Card island = new Island();
        harness.setLibrary(player1, List.of(plains, forest, island, new GrizzlyBears()));
        gd.recordSpellCast(player1.getId(), new GrizzlyBears());
        gd.recordSpellCast(player2.getId(), new GrizzlyBears());

        castSproutingVines();
        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(2);
        resolveSearchFromTopOfStack();
        resolveSearchFromTopOfStack();
        resolveSearchFromTopOfStack();

        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactlyInAnyOrder(plains, forest, island);
        harness.assertInGraveyard(player1, "Sprouting Vines");
    }

    @Test
    @DisplayName("Resolves without finding a basic land when the library has none")
    void resolvesWithoutBasicLand() {
        Card nonBasicCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(nonBasicCard));

        castSproutingVines();
        resolveStormAndSpell();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonBasicCard);
        harness.assertInGraveyard(player1, "Sprouting Vines");
    }

    @Test
    @DisplayName("Storm creates one copy for each spell cast before Sprouting Vines")
    void stormCopiesForEachPriorSpell() {
        gd.recordSpellCast(player1.getId(), new GrizzlyBears());
        gd.recordSpellCast(player2.getId(), new GrizzlyBears());
        castSproutingVines();

        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(2);
    }

    @Test
    @DisplayName("May fail to find even when a basic land is available")
    void mayFailToFindAvailableBasicLand() {
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        castSproutingVines();
        resolveStormAndSpell();

        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        harness.assertInGraveyard(player1, "Sprouting Vines");
    }

    @Test
    @DisplayName("The first spell of the turn creates no storm copies")
    void firstSpellCreatesNoCopies() {
        harness.setLibrary(player1, List.of(new Forest()));
        castSproutingVines();

        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).isEmpty();
        resolveSearchFromTopOfStack();
        harness.assertInHand(player1, "Forest");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Spells cast after Sprouting Vines do not increase its storm count")
    void laterSpellsDoNotIncreaseStormCount() {
        gd.recordSpellCast(player2.getId(), new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest(), new Plains()));
        castSproutingVines();
        gd.recordSpellCast(player2.getId(), new GrizzlyBears());

        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(1);
        resolveSearchFromTopOfStack();
        resolveSearchFromTopOfStack();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Sprouting Vines")).hasSize(1);
    }

    @Test
    @DisplayName("Storm searches finish normally after the available basic land is exhausted")
    void stormCopiesCanExhaustBasicLands() {
        Card forest = new Forest();
        Card nonLand = new GrizzlyBears();
        harness.setLibrary(player1, List.of(forest, nonLand));
        gd.recordSpellCast(player2.getId(), new GrizzlyBears());
        castSproutingVines();
        harness.passBothPriorities();
        resolveSearchFromTopOfStack();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonLand);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Sprouting Vines");
    }

    private void castSproutingVines() {
        harness.castFromHand(player1, new SproutingVines(), "{2}{G}");
    }

    private void resolveStormAndSpell() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void resolveSearchFromTopOfStack() {
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 0);
    }
}
