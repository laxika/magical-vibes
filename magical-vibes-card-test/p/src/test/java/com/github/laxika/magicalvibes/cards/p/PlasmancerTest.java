package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.cards.s.SunkenHollow;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Plasmancer.class, Swamp.class, Forest.class, SunkenHollow.class, GrizzlyBears.class})
class PlasmancerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB searches for a basic Swamp and puts it into hand")
    void etbSearchesForBasicSwamp() {
        Swamp swamp = new Swamp();
        Forest forest = new Forest();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(swamp, forest, bears));
        castPlasmancer();

        resolveEtb();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(swamp);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.HAND);
        assertThat(search.params().reveals()).isTrue();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(swamp);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(forest, bears);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("ETB does not find a non-Swamp card")
    void etbDoesNotFindNonSwamp() {
        Forest forest = new Forest();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(forest, bears));
        castPlasmancer();

        resolveEtb();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(forest, bears);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(forest, bears);
    }

    @Test
    @DisplayName("ETB excludes nonbasic Swamps and finds only one basic Swamp")
    void etbExcludesNonbasicSwampsAndFindsOnlyOneCard() {
        Swamp first = new Swamp();
        Swamp second = new Swamp();
        SunkenHollow hollow = new SunkenHollow();
        harness.setLibrary(player1, List.of(first, hollow, second));
        castPlasmancer();
        resolveEtb();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(first, second);

        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(first, hollow);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("ETB allows failing to find even when a basic Swamp is available")
    void etbAllowsFailingToFind() {
        Swamp swamp = new Swamp();
        harness.setLibrary(player1, List.of(swamp));
        castPlasmancer();
        resolveEtb();

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(swamp);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("ETB resolves with an empty library without searching the opponent's library")
    void etbResolvesWithEmptyLibrary() {
        Swamp opponentSwamp = new Swamp();
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of(opponentSwamp));
        castPlasmancer();
        resolveEtb();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentSwamp);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private void castPlasmancer() {
        harness.setHand(player1, List.of(new Plasmancer()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
    }

    private void resolveEtb() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
