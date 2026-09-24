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
