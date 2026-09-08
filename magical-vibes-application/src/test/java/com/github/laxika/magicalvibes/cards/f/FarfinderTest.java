package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({Farfinder.class, Forest.class, GrizzlyBears.class})
class FarfinderTest extends BaseCardTest {

    @Test
    @DisplayName("The ETB may search for a basic land and put it into its controller's hand")
    void maySearchForBasicLand() {
        Forest forest = new Forest();
        setUpLibrary(forest, new GrizzlyBears());
        castFarfinder();

        resolveToMayChoice();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(forest);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.HAND);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).contains(forest);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining the ETB search leaves the library unchanged")
    void mayDeclineSearch() {
        Forest forest = new Forest();
        setUpLibrary(forest);
        castFarfinder();

        resolveToMayChoice();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(forest);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The optional search does nothing when the library has no basic land")
    void noBasicLandToFind() {
        GrizzlyBears bears = new GrizzlyBears();
        setUpLibrary(bears);
        castFarfinder();

        resolveToMayChoice();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bears);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castFarfinder() {
        harness.setHand(player1, List.of(new Farfinder()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
    }

    private void resolveToMayChoice() {
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    private void setUpLibrary(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }
}
