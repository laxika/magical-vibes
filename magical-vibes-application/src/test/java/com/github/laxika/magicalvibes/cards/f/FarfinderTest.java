package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.e.EvolvingWilds;
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

@CardUsed({Farfinder.class, Forest.class, EvolvingWilds.class})
class FarfinderTest extends BaseCardTest {

    @Test
    @DisplayName("The ETB may search for a basic land and put it into its controller's hand")
    void maySearchForBasicLand() {
        Forest forest = new Forest();
        setUpLibrary(forest, new Farfinder());
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
        Farfinder otherFarfinder = new Farfinder();
        setUpLibrary(otherFarfinder);
        castFarfinder();

        resolveToMayChoice();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(otherFarfinder);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The search excludes nonbasic lands and reveals the chosen basic land")
    void excludesNonbasicLandsAndRevealsFoundLand() {
        Forest forest = new Forest();
        EvolvingWilds wilds = new EvolvingWilds();
        setUpLibrary(wilds, forest);
        castFarfinder();
        resolveToMayChoice();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(forest);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(wilds);
        assertThat(gameLogContains("reveals Forest and puts it into their hand")).isTrue();
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The controller may fail to find even when a basic land is available")
    void mayFailToFindBasicLand() {
        Forest forest = new Forest();
        setUpLibrary(forest);
        castFarfinder();
        resolveToMayChoice();
        harness.handleMayAbilityChosen(player1, true);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gameLogContains("chooses not to take a card. Library is shuffled.")).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Accepting the search with an empty library finishes the ability")
    void maySearchEmptyLibrary() {
        setUpLibrary();
        castFarfinder();
        resolveToMayChoice();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Farfinder attacks without tapping")
    void vigilanceKeepsAttackerUntapped() {
        var farfinder = addCreatureReady(player1, new Farfinder());

        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(farfinder.isTapped()).isFalse();
        harness.assertLife(player2, 19);
    }

    private void castFarfinder() {
        harness.setHand(player1, List.of(new Farfinder()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
    }

    private void resolveToMayChoice() {
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    private void setUpLibrary(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }
}
