package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TraverseTheOutlands.class, Forest.class, Island.class, Mountain.class,
        GrizzlyBears.class, HillGiant.class})
class TraverseTheOutlandsTest extends BaseCardTest {

    @Test
    @DisplayName("Searches for up to the greatest power in basic lands and puts them onto the battlefield tapped")
    void searchesForBasicLandsEqualToGreatestPower() {
        harness.addToBattlefield(player1, new HillGiant());
        List<Card> library = List.of(new Forest(), new Island(), new Mountain(), new GrizzlyBears());
        harness.setLibrary(player1, library);
        castTraverseTheOutlands();

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(library.get(0), library.get(1), library.get(2));
        assertThat(search.params().cards()).allMatch(card -> card.hasType(CardType.LAND));
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
        assertThat(search.params().remainingCount()).isEqualTo(3);

        for (int i = 0; i < 3; i++) {
            harness.getGameService().handleInteractionAnswer(
                    gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        }

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanents(player1, "Forest").getFirst().isTapped()).isTrue();
        assertThat(findPermanents(player1, "Island").getFirst().isTapped()).isTrue();
        assertThat(findPermanents(player1, "Mountain").getFirst().isTapped()).isTrue();
        assertThat(findPermanents(player1, "Grizzly Bears")).isEmpty();
    }

    @Test
    @DisplayName("Can find fewer than the greatest power")
    void canFindFewerThanTheGreatestPower() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.setLibrary(player1, List.of(new Forest(), new Island()));
        castTraverseTheOutlands();

        harness.passBothPriorities();
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanents(player1, "Forest")).hasSize(1);
        assertThat(findPermanents(player1, "Forest").getFirst().isTapped()).isTrue();
        assertThat(findPermanents(player1, "Island")).isEmpty();
    }

    private void castTraverseTheOutlands() {
        harness.setHand(player1, List.of(new TraverseTheOutlands()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castSorcery(player1, 0, 0);
    }
}
