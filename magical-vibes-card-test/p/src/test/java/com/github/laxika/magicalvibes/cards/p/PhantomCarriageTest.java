package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BaithookAngler;
import com.github.laxika.magicalvibes.cards.h.HookHauntDrifter;
import com.github.laxika.magicalvibes.cards.o.OtherworldlyGaze;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PhantomCarriage.class, OtherworldlyGaze.class, BaithookAngler.class, HookHauntDrifter.class})
class PhantomCarriageTest extends BaseCardTest {

    @Test
    @DisplayName("ETB search offers cards with flashback or disturb")
    void searchOffersFlashbackOrDisturbCards() {
        castPhantomCarriage();
        harness.setLibrary(player1, List.of(new OtherworldlyGaze(), new BaithookAngler(), new PhantomCarriage()));

        resolveCarriageAndAcceptSearch();

        List<Card> offered = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards();
        assertThat(offered).extracting(Card::getName)
                .containsExactly("Otherworldly Gaze", "Baithook Angler");
    }

    @Test
    @DisplayName("Choosing a matching card puts it into the graveyard")
    void chosenMatchingCardGoesToGraveyard() {
        castPhantomCarriage();
        Card disturbCard = new BaithookAngler();
        harness.setLibrary(player1, List.of(new PhantomCarriage(), disturbCard));

        resolveCarriageAndAcceptSearch();
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Baithook Angler");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining the search does not move a card")
    void decliningSearchDoesNothing() {
        castPhantomCarriage();
        harness.setLibrary(player1, List.of(new OtherworldlyGaze()));

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Choosing a flashback card moves only that card from the controller's library")
    void chosenFlashbackCardGoesToGraveyard() {
        castPhantomCarriage();
        Card flashbackCard = new OtherworldlyGaze();
        Card disturbCard = new BaithookAngler();
        Card opponentsCard = new OtherworldlyGaze();
        harness.setLibrary(player1, List.of(flashbackCard, disturbCard));
        harness.setLibrary(player2, List.of(opponentsCard));

        resolveCarriageAndAcceptSearch();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(flashbackCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(disturbCard);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentsCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A restricted search can fail to find even with a matching card")
    void canFailToFindMatchingCard() {
        castPhantomCarriage();
        Card matchingCard = new OtherworldlyGaze();
        harness.setLibrary(player1, List.of(matchingCard));

        resolveCarriageAndAcceptSearch();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(matchingCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Searching a library with no matching cards completes without moving cards")
    void noMatchingCardsCompletesSearch() {
        castPhantomCarriage();
        Card nonMatchingCard = new PhantomCarriage();
        harness.setLibrary(player1, List.of(nonMatchingCard));

        resolveCarriageAndAcceptSearch();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonMatchingCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Searching an empty library completes without a choice")
    void emptyLibraryCompletesSearch() {
        castPhantomCarriage();
        harness.setLibrary(player1, List.of());

        resolveCarriageAndAcceptSearch();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castPhantomCarriage() {
        harness.setHand(player1, List.of(new PhantomCarriage()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
    }

    private void resolveCarriageAndAcceptSearch() {
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
    }
}
