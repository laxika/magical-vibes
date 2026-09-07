package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PathToTheFestival.class, Forest.class, GrizzlyBears.class, Island.class, Mountain.class})
class PathToTheFestivalTest extends BaseCardTest {

    @Test
    @DisplayName("Searches for a basic land onto the battlefield tapped and scries when Domain is met")
    void searchesAndScriesWithThreeBasicLandTypes() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        harness.setLibrary(player1, List.of(new Mountain(), new GrizzlyBears()));
        castPathToTheFestival();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        Permanent mountain = findPermanent(player1, "Mountain");
        assertThat(mountain.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
    }

    @Test
    @DisplayName("Does not scry when fewer than three basic land types are controlled after the search")
    void doesNotScryWithFewerThanThreeBasicLandTypes() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears()));
        castPathToTheFestival();

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Can fail to find a basic land and skips the Domain scry when below three types")
    void failingToFindSkipsScryBelowThreeTypes() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        castPathToTheFestival();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castPathToTheFestival() {
        harness.setHand(player1, List.of(new PathToTheFestival()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
