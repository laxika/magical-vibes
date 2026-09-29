package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RyanSinclair.class, Forest.class, GrizzlyBears.class, HillGiant.class})
class RyanSinclairTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking exiles through lands and offers the first nonland within Ryan's power")
    void offersFirstEligibleNonland() {
        Forest land = new Forest();
        GrizzlyBears bears = new GrizzlyBears();
        readyRyan();
        harness.setLibrary(player1, List.of(land, bears));

        declareAttackers(List.of(0));
        resolveAllTriggers();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(bears);
        assertThat(search.params().destination())
                .isEqualTo(LibrarySearchDestination.CAST_WITHOUT_PAYING);
    }

    @Test
    @DisplayName("Stops at the first nonland even when that card is too expensive")
    void stopsAtFirstNonland() {
        Forest land = new Forest();
        HillGiant tooExpensive = new HillGiant();
        GrizzlyBears laterCard = new GrizzlyBears();
        readyRyan();
        harness.setLibrary(player1, List.of(land, tooExpensive, laterCard));

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(land, tooExpensive, laterCard);
    }

    @Test
    @DisplayName("The offered card is cast without paying mana")
    void castsOfferedCardForFree() {
        GrizzlyBears bears = new GrizzlyBears();
        readyRyan();
        harness.setLibrary(player1, List.of(bears));

        declareAttackers(List.of(0));
        resolveAllTriggers();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == bears);
    }

    private void readyRyan() {
        harness.addToBattlefield(player1, new RyanSinclair());
        gd.playerBattlefields.get(player1.getId()).getLast().setSummoningSick(false);
    }
}
