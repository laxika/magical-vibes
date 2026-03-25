package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.DealDamageToControllerEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsChooseNToHandRestToGraveyardEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DarkBargainTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Dark Bargain has correct effect structure")
    void hasCorrectProperties() {
        DarkBargain card = new DarkBargain();

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0))
                .isInstanceOf(LookAtTopCardsChooseNToHandRestToGraveyardEffect.class);
        LookAtTopCardsChooseNToHandRestToGraveyardEffect lookEffect =
                (LookAtTopCardsChooseNToHandRestToGraveyardEffect) card.getEffects(EffectSlot.SPELL).get(0);
        assertThat(lookEffect.count()).isEqualTo(3);
        assertThat(lookEffect.toHandCount()).isEqualTo(2);

        assertThat(card.getEffects(EffectSlot.SPELL).get(1))
                .isInstanceOf(DealDamageToControllerEffect.class);
        DealDamageToControllerEffect dmgEffect =
                (DealDamageToControllerEffect) card.getEffects(EffectSlot.SPELL).get(1);
        assertThat(dmgEffect.damage()).isEqualTo(2);
    }

    // ===== Casting =====

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
        assertThat(entry.getCard().getName()).isEqualTo("Dark Bargain");
        assertThat(entry.getControllerId()).isEqualTo(player1.getId());
    }

    // ===== Resolving with 3 cards =====

    @Test
    @DisplayName("Resolving enters library reveal choice state")
    void resolvingEntersRevealChoiceState() {
        setupTopCards(List.of(new GrizzlyBears(), new Shock(), new GrizzlyBears()));

        harness.setHand(player1, List.of(new DarkBargain()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_REVEAL_CHOICE);
    }

    @Test
    @DisplayName("Dark Bargain goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        setupTopCards(List.of(new GrizzlyBears(), new Shock(), new GrizzlyBears()));

        harness.setHand(player1, List.of(new DarkBargain()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Dark Bargain"));
        assertThat(gd.stack).isEmpty();
    }

    // ===== Choosing cards =====

    @Test
    @DisplayName("Choosing two cards puts them in hand and the third into graveyard")
    void choosingTwoPutsInHandOneInGraveyard() {
        Card card0 = new GrizzlyBears();
        Card card1 = new Shock();
        Card card2 = new GrizzlyBears();
        setupTopCards(List.of(card0, card1, card2));

        harness.setHand(player1, List.of(new DarkBargain()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

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
        Card card0 = new GrizzlyBears();
        Card card1 = new Shock();
        Card card2 = new GrizzlyBears();
        setupTopCards(List.of(card0, card1, card2));

        harness.setHand(player1, List.of(new DarkBargain()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

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
        Card card0 = new GrizzlyBears();
        Card card1 = new GrizzlyBears();
        Card card2 = new GrizzlyBears();
        setupTopCards(List.of(card0, card1, card2));

        harness.setHand(player1, List.of(new DarkBargain()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.handleMultipleCardsChosen(player1, List.of(card0.getId(), card1.getId()));

        assertThat(gd.interaction.awaitingInputType()).isNull();
    }

    @Test
    @DisplayName("Remaining card does not stay in library")
    void remainingCardNotInLibrary() {
        Card card0 = new GrizzlyBears();
        Card card1 = new Shock();
        Card card2 = new GrizzlyBears();
        setupTopCards(List.of(card0, card1, card2));

        harness.setHand(player1, List.of(new DarkBargain()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.handleMultipleCardsChosen(player1, List.of(card0.getId(), card1.getId()));

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    // ===== Self-damage =====

    @Test
    @DisplayName("Dark Bargain deals 2 damage to its controller after card choice")
    void dealsTwoDamageToController() {
        Card card0 = new GrizzlyBears();
        Card card1 = new Shock();
        Card card2 = new GrizzlyBears();
        setupTopCards(List.of(card0, card1, card2));

        harness.setHand(player1, List.of(new DarkBargain()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        // Damage hasn't happened yet — still waiting for card choice
        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);

        // Make the choice, which resumes effect resolution including the damage
        harness.handleMultipleCardsChosen(player1, List.of(card0.getId(), card1.getId()));

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 2);
    }

    // ===== Library edge cases =====

    @Test
    @DisplayName("With 2 cards in library, both go directly to hand (no choice needed)")
    void twoCardsInLibraryBothGoToHand() {
        GameData gd = harness.getGameData();
        gd.playerDecks.get(player1.getId()).clear();
        Card cardA = new GrizzlyBears();
        Card cardB = new Shock();
        gd.playerDecks.get(player1.getId()).add(cardA);
        gd.playerDecks.get(player1.getId()).add(cardB);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.setHand(player1, List.of(new DarkBargain()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        // Both cards should go directly to hand (2 cards <= toHandCount of 2)
        assertThat(gd.interaction.awaitingInputType()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(cardA, cardB);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        // Self-damage should still apply
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("With 1 card in library, it goes directly to hand")
    void oneCardInLibrary() {
        GameData gd = harness.getGameData();
        gd.playerDecks.get(player1.getId()).clear();
        Card singleCard = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).add(singleCard);

        harness.setHand(player1, List.of(new DarkBargain()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(singleCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("With empty library, nothing happens but self-damage still applies")
    void emptyLibrary() {
        GameData gd = harness.getGameData();
        gd.playerDecks.get(player1.getId()).clear();

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.setHand(player1, List.of(new DarkBargain()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        // Self-damage should still apply even with empty library
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 2);
    }

    // ===== Game log =====

    @Test
    @DisplayName("Game log records looking at cards")
    void gameLogRecordsLooking() {
        setupTopCards(List.of(new GrizzlyBears(), new Shock(), new GrizzlyBears()));

        harness.setHand(player1, List.of(new DarkBargain()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("looks at the top") && log.contains("3"));
    }

    @Test
    @DisplayName("Game log records putting cards in hand and rest in graveyard")
    void gameLogRecordsChoice() {
        Card card0 = new GrizzlyBears();
        Card card1 = new Shock();
        Card card2 = new GrizzlyBears();
        setupTopCards(List.of(card0, card1, card2));

        harness.setHand(player1, List.of(new DarkBargain()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.handleMultipleCardsChosen(player1, List.of(card0.getId(), card1.getId()));

        assertThat(gd.gameLog).anyMatch(log ->
                log.contains("puts 2 cards into their hand") && log.contains("graveyard"));
    }

    private void setupTopCards(List<Card> cards) {
        List<Card> deck = harness.getGameData().playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(cards);
    }
}
