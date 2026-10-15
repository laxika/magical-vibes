package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.CrumblingVestige;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.w.Wastes;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LoamLarva.class, Forest.class, Plains.class, Island.class, GrizzlyBears.class,
        Wastes.class, CrumblingVestige.class})
class LoamLarvaTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the ETB ability offers only basic lands and puts the choice on top")
    void acceptingEtbAbilityPutsBasicLandOnTop() {
        setupAndCast();
        Forest forest = new Forest();
        Plains plains = new Plains();
        Island island = new Island();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(forest, plains, island, bears));

        resolveToMayPrompt();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactlyInAnyOrder(forest, plains, island);

        String chosenName = search.params().cards().getFirst().getName();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getName()).isEqualTo(chosenName);
        assertThat(gd.playerDecks.get(player1.getId())).contains(bears);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining the ETB ability skips the library search")
    void decliningEtbAbilitySkipsSearch() {
        setupAndCast();
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard, new Forest()));

        resolveToMayPrompt();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(topCard);
    }

    @Test
    @DisplayName("A restricted search may fail to find even with a basic land available")
    void mayFailToFindAvailableBasicLand() {
        setupAndCast();
        Forest forest = new Forest();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(forest, bears));

        resolveToMayPrompt();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(forest, bears);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Accepting the search with no basic lands finishes without moving any cards")
    void searchWithNoBasicLandsFinishes() {
        setupAndCast();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bears));

        resolveToMayPrompt();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bears);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Accepting the search with an empty library finishes normally")
    void searchWithEmptyLibraryFinishes() {
        setupAndCast();
        harness.setLibrary(player1, List.of());

        resolveToMayPrompt();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Wastes qualifies as a basic land, but nonbasic lands do not")
    void findsWastesButNotNonbasicLand() {
        setupAndCast();
        Wastes wastes = new Wastes();
        CrumblingVestige vestige = new CrumblingVestige();
        LoamLarva otherLarva = new LoamLarva();
        harness.setLibrary(player1, List.of(vestige, otherLarva, wastes));
        harness.setLibrary(player2, List.of(new Wastes()));
        List<Card> opponentLibrary = List.copyOf(gd.playerDecks.get(player2.getId()));

        resolveToMayPrompt();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(wastes);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(wastes);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(wastes, vestige, otherLarva);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyElementsOf(opponentLibrary);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private void setupAndCast() {
        harness.setHand(player1, List.of(new LoamLarva()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
    }

    private void resolveToMayPrompt() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
