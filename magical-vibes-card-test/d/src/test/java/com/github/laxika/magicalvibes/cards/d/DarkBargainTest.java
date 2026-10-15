package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.p.PrimordialWurm;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DarkBargain.class, PrimordialWurm.class, Opt.class})
class DarkBargainTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Dark Bargain puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new DarkBargain()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard()).isInstanceOf(DarkBargain.class);
        assertThat(entry.getControllerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Resolving enters library reveal choice state")
    void resolvingEntersRevealChoiceState() {
        setupTopCards(List.of(new PrimordialWurm(), new Opt(), new PrimordialWurm()));

        harness.setHand(player1, List.of(new DarkBargain()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
    }

    @Test
    @DisplayName("Dark Bargain goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        Card card0 = new PrimordialWurm();
        Card card1 = new Opt();
        setupTopCards(List.of(card0, card1, new PrimordialWurm()));

        harness.setHand(player1, List.of(new DarkBargain()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0);

        GameData gd = harness.getGameData();
        // The spell only reaches the graveyard once its resolution finishes
        harness.handleMultipleCardsChosen(player1, List.of(card0.getId(), card1.getId()));

        harness.assertInGraveyard(player1, "Dark Bargain");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Choosing two cards puts them in hand and the third into graveyard")
    void choosingTwoPutsInHandOneInGraveyard() {
        Card card0 = new PrimordialWurm();
        Card card1 = new Opt();
        Card card2 = new PrimordialWurm();
        setupTopCards(List.of(card0, card1, card2));

        harness.setHand(player1, List.of(new DarkBargain()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0);

        GameData gd = harness.getGameData();
        // Choose card0 and card1 for hand
        harness.handleMultipleCardsChosen(player1, List.of(card0.getId(), card1.getId()));

        // Hand should contain the two chosen cards
        assertThat(gd.playerHands.get(player1.getId())).contains(card0, card1);

        // card2 should be in the graveyard (plus Dark Bargain itself)
        List<Card> graveyard = gd.playerGraveyards.get(player1.getId());
        assertThat(graveyard).contains(card2);
        assertThat(graveyard).noneMatch(c -> c.getId().equals(card0.getId()));
        assertThat(graveyard).noneMatch(c -> c.getId().equals(card1.getId()));
    }

    @Test
    @DisplayName("Choosing different two cards works correctly")
    void choosingDifferentTwoCards() {
        Card card0 = new PrimordialWurm();
        Card card1 = new Opt();
        Card card2 = new PrimordialWurm();
        setupTopCards(List.of(card0, card1, card2));

        harness.setHand(player1, List.of(new DarkBargain()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0);

        GameData gd = harness.getGameData();
        // Choose card1 and card2 for hand
        harness.handleMultipleCardsChosen(player1, List.of(card1.getId(), card2.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(card1, card2);
        List<Card> graveyard = gd.playerGraveyards.get(player1.getId());
        assertThat(graveyard).contains(card0);
    }

    @Test
    @DisplayName("Choosing clears awaiting state")
    void choosingClearsAwaitingState() {
        Card card0 = new PrimordialWurm();
        Card card1 = new PrimordialWurm();
        Card card2 = new PrimordialWurm();
        setupTopCards(List.of(card0, card1, card2));

        harness.setHand(player1, List.of(new DarkBargain()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0);

        GameData gd = harness.getGameData();
        harness.handleMultipleCardsChosen(player1, List.of(card0.getId(), card1.getId()));

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Remaining card does not stay in library")
    void remainingCardNotInLibrary() {
        Card card0 = new PrimordialWurm();
        Card card1 = new Opt();
        Card card2 = new PrimordialWurm();
        setupTopCards(List.of(card0, card1, card2));

        harness.setHand(player1, List.of(new DarkBargain()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0);

        GameData gd = harness.getGameData();
        harness.handleMultipleCardsChosen(player1, List.of(card0.getId(), card1.getId()));

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Dark Bargain deals 2 damage to its controller after card choice")
    void dealsTwoDamageToController() {
        Card card0 = new PrimordialWurm();
        Card card1 = new Opt();
        Card card2 = new PrimordialWurm();
        setupTopCards(List.of(card0, card1, card2));

        harness.setHand(player1, List.of(new DarkBargain()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castAndResolveInstant(player1, 0);

        // Damage hasn't happened yet — still waiting for card choice
        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);

        // Make the choice, which resumes effect resolution including the damage
        harness.handleMultipleCardsChosen(player1, List.of(card0.getId(), card1.getId()));

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("With 2 cards in library, both go directly to hand (no choice needed)")
    void twoCardsInLibraryBothGoToHand() {
        GameData gd = harness.getGameData();
        Card cardA = new PrimordialWurm();
        Card cardB = new Opt();
        harness.setLibrary(player1, List.of(cardA, cardB));

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.setHand(player1, List.of(new DarkBargain()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0);

        // Both cards should go directly to hand (2 cards <= toHandCount of 2)
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(cardA, cardB);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        // Self-damage should still apply
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("With 1 card in library, it goes directly to hand")
    void oneCardInLibrary() {
        GameData gd = harness.getGameData();
        Card singleCard = new PrimordialWurm();
        harness.setLibrary(player1, List.of(singleCard));

        harness.setHand(player1, List.of(new DarkBargain()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(singleCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("With empty library, nothing happens but self-damage still applies")
    void emptyLibrary() {
        GameData gd = harness.getGameData();
        harness.setLibrary(player1, List.of());

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.setHand(player1, List.of(new DarkBargain()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        // Self-damage should still apply even with empty library
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("Game log records looking at cards")
    void gameLogRecordsLooking() {
        setupTopCards(List.of(new PrimordialWurm(), new Opt(), new PrimordialWurm()));

        harness.setHand(player1, List.of(new DarkBargain()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("looks at the top") && log.contains("3"));
    }

    @Test
    @DisplayName("Game log records putting cards in hand and rest in graveyard")
    void gameLogRecordsChoice() {
        Card card0 = new PrimordialWurm();
        Card card1 = new Opt();
        Card card2 = new PrimordialWurm();
        setupTopCards(List.of(card0, card1, card2));

        harness.setHand(player1, List.of(new DarkBargain()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0);

        GameData gd = harness.getGameData();
        harness.handleMultipleCardsChosen(player1, List.of(card0.getId(), card1.getId()));

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log ->
                log.contains("puts 2 cards into their hand") && log.contains("graveyard"));
    }

    @Test
    void cannotChooseNoCardsWhenThreeAreAvailable() {
        assertTooFewCardsRejected(false);
    }

    @Test
    void cannotChooseOnlyOneCardWhenThreeAreAvailable() {
        assertTooFewCardsRejected(true);
    }

    private void assertTooFewCardsRejected(boolean chooseOne) {
        Card first = new PrimordialWurm();
        Card second = new Opt();
        Card third = new PrimordialWurm();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.setHand(player1, List.of(new DarkBargain()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castAndResolveInstant(player1, 0);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                chooseOne ? List.of(first.getId()) : List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        harness.assertLife(player1, 20);

        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(third);
        harness.assertLife(player1, 18);
    }

    @Test
    void leavesCardsBelowTopThreeInTheirOriginalOrder() {
        Card first = new PrimordialWurm();
        Card second = new Opt();
        Card third = new PrimordialWurm();
        Card fourth = new Opt();
        Card fifth = new PrimordialWurm();
        harness.setLibrary(player1, List.of(first, second, third, fourth, fifth));
        harness.setHand(player1, List.of(new DarkBargain()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castAndResolveInstant(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), third.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(first, third);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fourth, fifth);
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    @Test
    void otherPlayerUsesTheirOwnLibraryAndTakesTheDamage() {
        Card first = new PrimordialWurm();
        Card second = new Opt();
        Card third = new PrimordialWurm();
        Card opponentCard = new Opt();
        harness.setLibrary(player2, List.of(first, second, third));
        harness.setLibrary(player1, List.of(opponentCard));
        harness.setHand(player2, List.of(new DarkBargain()));
        harness.addMana(player2, ManaColor.BLACK, 4);
        harness.castAndResolveInstant(player2, 0);
        harness.handleMultipleCardsChosen(player2, List.of(first.getId(), second.getId()));

        assertThat(gd.playerHands.get(player2.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(third);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(opponentCard);
        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
    }

    private void setupTopCards(List<Card> cards) {
        harness.setLibrary(player1, cards);
    }
}
