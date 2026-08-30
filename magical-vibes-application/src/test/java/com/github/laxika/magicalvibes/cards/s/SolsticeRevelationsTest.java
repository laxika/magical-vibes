package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SolsticeRevelations.class, Mountain.class, GrizzlyBears.class})
class SolsticeRevelationsTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles until a nonland card and offers it when its mana value is below the Mountain count")
    void offersEligibleCardForFreeCast() {
        addMountains(3);
        GrizzlyBears hit = new GrizzlyBears();
        Mountain revealedLand = new Mountain();
        castSolstice(List.of(revealedLand, hit));

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(hit);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(revealedLand, hit);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.stack).anyMatch(entry -> entry.getCard() == hit
                && entry.getEntryType() == StackEntryType.CREATURE_SPELL);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(revealedLand);
    }

    @Test
    @DisplayName("Puts the nonland card into hand when its mana value is not below the Mountain count")
    void putsIneligibleCardIntoHand() {
        addMountains(2);
        GrizzlyBears hit = new GrizzlyBears();
        Mountain revealedLand = new Mountain();
        castSolstice(List.of(revealedLand, hit));

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(revealedLand);
    }

    @Test
    @DisplayName("Declining an eligible free cast puts the nonland card into hand")
    void declinePutsCardIntoHand() {
        addMountains(3);
        GrizzlyBears hit = new GrizzlyBears();
        Mountain revealedLand = new Mountain();
        castSolstice(List.of(revealedLand, hit));

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.stack).noneMatch(entry -> entry.getCard() == hit);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(revealedLand);
    }

    @Test
    @DisplayName("Exiles the whole library when no nonland card is found")
    void exilesAllCardsWhenNoNonlandCardIsFound() {
        addMountains(1);
        Mountain first = new Mountain();
        Mountain second = new Mountain();
        castSolstice(List.of(first, second));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(first, second);
    }

    private void castSolstice(List<Card> library) {
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new SolsticeRevelations()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }

    private void addMountains(int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player1, new Mountain());
        }
    }
}
