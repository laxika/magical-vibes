package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsChooseNToHandRestToGraveyardEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ForbiddenAlchemyTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Forbidden Alchemy has correct effect structure")
    void hasCorrectProperties() {
        ForbiddenAlchemy card = new ForbiddenAlchemy();

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst())
                .isInstanceOf(LookAtTopCardsChooseNToHandRestToGraveyardEffect.class);
        LookAtTopCardsChooseNToHandRestToGraveyardEffect effect =
                (LookAtTopCardsChooseNToHandRestToGraveyardEffect) card.getEffects(EffectSlot.SPELL).getFirst();
        assertThat(effect.count()).isEqualTo(4);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Forbidden Alchemy puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new ForbiddenAlchemy()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Forbidden Alchemy");
        assertThat(entry.getControllerId()).isEqualTo(player1.getId());
    }

    // ===== Resolving with 4 cards =====

    @Test
    @DisplayName("Resolving enters library reveal choice state")
    void resolvingEntersRevealChoiceState() {
        harness.setHand(player1, List.of(new ForbiddenAlchemy()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_REVEAL_CHOICE);
    }

    @Test
    @DisplayName("Forbidden Alchemy goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.setHand(player1, List.of(new ForbiddenAlchemy()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Forbidden Alchemy"));
        assertThat(gd.stack).isEmpty();
    }

    // ===== Choosing cards =====

    @Test
    @DisplayName("Choosing a card puts it in hand and the rest into graveyard")
    void choosingPutsOneInHandRestInGraveyard() {
        Card card0 = new GrizzlyBears();
        Card card1 = new Shock();
        Card card2 = new GrizzlyBears();
        Card card3 = new Shock();
        setupTopCards(List.of(card0, card1, card2, card3));

        harness.setHand(player1, List.of(new ForbiddenAlchemy()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Choose card1 (Shock) for hand
        harness.handleMultipleGraveyardCardsChosen(player1, List.of(card1.getId()));

        // Hand should contain the chosen card
        assertThat(gd.playerHands.get(player1.getId())).contains(card1);

        // The other 3 should be in the graveyard (plus Forbidden Alchemy itself)
        List<Card> graveyard = gd.playerGraveyards.get(player1.getId());
        assertThat(graveyard).contains(card0);
        assertThat(graveyard).contains(card2);
        assertThat(graveyard).contains(card3);
    }

    @Test
    @DisplayName("Choosing the first card puts it in hand and rest into graveyard")
    void choosingFirstCard() {
        Card card0 = new GrizzlyBears();
        Card card1 = new Shock();
        Card card2 = new GrizzlyBears();
        Card card3 = new Shock();
        setupTopCards(List.of(card0, card1, card2, card3));

        harness.setHand(player1, List.of(new ForbiddenAlchemy()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.handleMultipleGraveyardCardsChosen(player1, List.of(card0.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(card0);
        List<Card> graveyard = gd.playerGraveyards.get(player1.getId());
        assertThat(graveyard).contains(card1);
        assertThat(graveyard).contains(card2);
        assertThat(graveyard).contains(card3);
    }

    @Test
    @DisplayName("Choosing clears awaiting state")
    void choosingClearsAwaitingState() {
        Card card0 = new GrizzlyBears();
        Card card1 = new GrizzlyBears();
        Card card2 = new GrizzlyBears();
        Card card3 = new GrizzlyBears();
        setupTopCards(List.of(card0, card1, card2, card3));

        harness.setHand(player1, List.of(new ForbiddenAlchemy()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.handleMultipleGraveyardCardsChosen(player1, List.of(card0.getId()));

        assertThat(gd.interaction.awaitingInputType()).isNull();
    }

    @Test
    @DisplayName("Remaining cards do not stay in library")
    void remainingCardsNotInLibrary() {
        Card card0 = new GrizzlyBears();
        Card card1 = new Shock();
        Card card2 = new GrizzlyBears();
        Card card3 = new Shock();
        setupTopCards(List.of(card0, card1, card2, card3));

        harness.setHand(player1, List.of(new ForbiddenAlchemy()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.handleMultipleGraveyardCardsChosen(player1, List.of(card0.getId()));

        // Library should be empty (we only put 4 cards in it, all were taken)
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    // ===== Library edge cases =====

    @Test
    @DisplayName("With 1 card in library, it automatically goes to hand")
    void oneCardInLibrary() {
        GameData gd = harness.getGameData();
        gd.playerDecks.get(player1.getId()).clear();
        Card singleCard = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).add(singleCard);

        harness.setHand(player1, List.of(new ForbiddenAlchemy()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(singleCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("looks at the top card"));
    }

    @Test
    @DisplayName("With 2 cards in library, enters reveal choice with 2 cards")
    void twoCardsInLibrary() {
        GameData gd = harness.getGameData();
        gd.playerDecks.get(player1.getId()).clear();
        Card cardA = new GrizzlyBears();
        Card cardB = new Shock();
        gd.playerDecks.get(player1.getId()).add(cardA);
        gd.playerDecks.get(player1.getId()).add(cardB);

        harness.setHand(player1, List.of(new ForbiddenAlchemy()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_REVEAL_CHOICE);

        // Choose cardA for hand
        harness.handleMultipleGraveyardCardsChosen(player1, List.of(cardA.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(cardA);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(cardB);
    }

    @Test
    @DisplayName("With empty library, nothing happens")
    void emptyLibrary() {
        GameData gd = harness.getGameData();
        gd.playerDecks.get(player1.getId()).clear();

        harness.setHand(player1, List.of(new ForbiddenAlchemy()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("library is empty"));
    }

    // ===== Flashback =====

    @Test
    @DisplayName("Flashback from graveyard works correctly")
    void flashbackFromGraveyard() {
        Card card0 = new GrizzlyBears();
        Card card1 = new Shock();
        Card card2 = new GrizzlyBears();
        Card card3 = new Shock();
        setupTopCards(List.of(card0, card1, card2, card3));

        harness.setGraveyard(player1, List.of(new ForbiddenAlchemy()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_REVEAL_CHOICE);

        harness.handleMultipleGraveyardCardsChosen(player1, List.of(card0.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(card0);
        List<Card> graveyard = gd.playerGraveyards.get(player1.getId());
        assertThat(graveyard).contains(card1);
        assertThat(graveyard).contains(card2);
        assertThat(graveyard).contains(card3);
    }

    @Test
    @DisplayName("Flashback exiles the spell after resolving")
    void flashbackExilesAfterResolving() {
        Card card0 = new GrizzlyBears();
        Card card1 = new GrizzlyBears();
        Card card2 = new GrizzlyBears();
        Card card3 = new GrizzlyBears();
        setupTopCards(List.of(card0, card1, card2, card3));

        harness.setGraveyard(player1, List.of(new ForbiddenAlchemy()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.handleMultipleGraveyardCardsChosen(player1, List.of(card0.getId()));

        // Should NOT be in graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Forbidden Alchemy"));
        // Should be in exile
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Forbidden Alchemy"));
    }

    // ===== Game log =====

    @Test
    @DisplayName("Game log records looking at cards")
    void gameLogRecordsLooking() {
        setupTopCards(List.of(new GrizzlyBears(), new Shock(), new GrizzlyBears(), new Shock()));

        harness.setHand(player1, List.of(new ForbiddenAlchemy()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("looks at the top") && log.contains("4"));
    }

    @Test
    @DisplayName("Game log records putting card in hand and rest in graveyard")
    void gameLogRecordsChoice() {
        Card card0 = new GrizzlyBears();
        setupTopCards(List.of(card0, new Shock(), new GrizzlyBears(), new Shock()));

        harness.setHand(player1, List.of(new ForbiddenAlchemy()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.handleMultipleGraveyardCardsChosen(player1, List.of(card0.getId()));

        assertThat(gd.gameLog).anyMatch(log ->
                log.contains("puts one card into their hand") && log.contains("graveyard"));
    }

    private void setupTopCards(List<Card> cards) {
        List<Card> deck = harness.getGameData().playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(cards);
    }
}
