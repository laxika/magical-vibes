package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({SavageLandDinosaur.class, Forest.class, GrizzlyBears.class})
class SavageLandDinosaurTest extends BaseCardTest {

    @Test
    @DisplayName("Basic landcycling searches for a basic land and discards the card")
    void basicLandcyclingSearchesForBasicLand() {
        Forest forest = new Forest();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(new SavageLandDinosaur()));
        harness.setLibrary(player1, List.of(forest, bears));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(forest);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.HAND);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertInGraveyard(player1, "Savage Land Dinosaur");
        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).noneMatch(card -> card instanceof SavageLandDinosaur);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bears);
    }
}
