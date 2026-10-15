package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CrawlingBarrens;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ReclaimTheWastes.class, Plains.class, Forest.class, Island.class, GrizzlyBears.class, CrawlingBarrens.class})
class ReclaimTheWastesTest extends BaseCardTest {

    @Test
    void searchesForOneBasicLandWithoutKicker() {
        cast(false);
        harness.setLibrary(player1, List.of(new Plains(), new Forest(), new Island(), new GrizzlyBears()));

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).hasSize(3)
                .allMatch(card -> card instanceof Plains || card instanceof Forest || card instanceof Island);
        assertThat(search.params().remainingCount()).isEqualTo(1);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card instanceof Plains || card instanceof Forest || card instanceof Island);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void kickedSearchesForUpToTwoBasicLands() {
        cast(true);
        harness.setLibrary(player1, List.of(new Plains(), new Forest(), new Island(), new GrizzlyBears()));

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        harness.handleCardChosen(player1, 0);

        PendingInteraction.LibrarySearch secondSearch =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(secondSearch).isNotNull();
        assertThat(secondSearch.params().remainingCount()).isEqualTo(1);
        assertThat(secondSearch.params().cards()).hasSize(2)
                .allMatch(card -> card instanceof Plains || card instanceof Forest || card instanceof Island);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId()).stream()
                .filter(card -> card instanceof Plains || card instanceof Forest || card instanceof Island))
                .hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void kickedSearchCanFindOnlyOneAvailableBasicLand() {
        cast(true);
        Forest forest = new Forest();
        CrawlingBarrens barrens = new CrawlingBarrens();
        harness.setLibrary(player1, List.of(forest, barrens));

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(forest);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(barrens);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(card -> card instanceof ReclaimTheWastes);
    }

    @Test
    void kickedSearchCanStopAfterOneEvenWhenAnotherBasicLandRemains() {
        cast(true);
        Forest forest = new Forest();
        Plains plains = new Plains();
        harness.setLibrary(player1, List.of(forest, plains));

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(plains);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void canFailToFindDespiteHavingABasicLand() {
        cast(false);
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));

        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void resolvesWithoutFindingCardsWhenLibraryHasNoBasicLands() {
        cast(true);
        CrawlingBarrens barrens = new CrawlingBarrens();
        harness.setLibrary(player1, List.of(barrens));

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(barrens);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(card -> card instanceof ReclaimTheWastes);
    }

    @Test
    void resolvesWithAnEmptyLibrary() {
        cast(false);
        harness.setLibrary(player1, List.of());

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(card -> card instanceof ReclaimTheWastes);
    }
    private void cast(boolean kicked) {
        harness.setHand(player1, List.of(new ReclaimTheWastes()));
        harness.addMana(player1, ManaColor.GREEN, kicked ? 4 : 1);
        if (kicked) {
            harness.castKickedSorcery(player1, 0);
        } else {
            harness.castSorcery(player1, 0, 0);
        }
    }
}
