package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OrganHoarder.class, Island.class})
class OrganHoarderTest extends BaseCardTest {

    @Test
    void enteringBattlefieldCreatesLibraryChoice() {
        harness.setLibrary(player1, List.of(new OrganHoarder(), new Island(), new OrganHoarder()));
        castAndResolveEtb();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.allCards()).hasSize(3);
    }

    @Test
    void choosingOnePutsItInHandAndRestInGraveyard() {
        Card card0 = new OrganHoarder();
        Card card1 = new Island();
        Card card2 = new OrganHoarder();
        harness.setLibrary(player1, List.of(card0, card1, card2));
        castAndResolveEtb();

        harness.handleMultipleCardsChosen(player1, List.of(card1.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(card1);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrder(card0, card2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void withOneCardInLibraryItAutomaticallyGoesToHand() {
        Card card = new Island();
        harness.setLibrary(player1, List.of(card));
        castAndResolveEtb();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(card);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void cannotDeclinePuttingOneOfTheLookedAtCardsIntoHand() {
        Card card0 = new OrganHoarder();
        Card card1 = new Island();
        Card card2 = new OrganHoarder();
        harness.setLibrary(player1, List.of(card0, card1, card2));
        castAndResolveEtb();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.interaction.activeInteraction()).isNotNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();

        harness.handleMultipleCardsChosen(player1, List.of(card0.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(card0);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrder(card1, card2);
    }

    @Test
    void withTwoCardsInLibraryChoosesOneAndPutsTheOtherIntoGraveyard() {
        Card chosen = new OrganHoarder();
        Card other = new Island();
        harness.setLibrary(player1, List.of(chosen, other));
        castAndResolveEtb();

        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(chosen);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(other);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void emptyLibraryResolvesWithoutAChoiceOrDrawingACard() {
        harness.setLibrary(player1, List.of());
        castAndResolveEtb();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Organ Hoarder");
    }

    @Test
    void onlyTopThreeCardsAreEligibleAndDeeperCardsStayInOrder() {
        Card card0 = new Island();
        Card card1 = new OrganHoarder();
        Card card2 = new Island();
        Card card3 = new OrganHoarder();
        Card card4 = new Island();
        harness.setLibrary(player1, List.of(card0, card1, card2, card3, card4));
        castAndResolveEtb();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(card3.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(card0.getId(), card1.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.handleMultipleCardsChosen(player1, List.of(card2.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(card2);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrder(card0, card1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(card3, card4);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castAndResolveEtb() {
        harness.setHand(player1, List.of(new OrganHoarder()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

}
