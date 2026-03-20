package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCardToHandAndCardToGraveyardEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FinalPartingTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Final Parting has SearchLibraryForCardToHandAndCardToGraveyardEffect as spell effect")
    void hasCorrectEffect() {
        FinalParting card = new FinalParting();

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst())
                .isInstanceOf(SearchLibraryForCardToHandAndCardToGraveyardEffect.class);
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting Final Parting puts it on the stack as a sorcery")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new FinalParting()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Final Parting");
    }

    @Test
    @DisplayName("Resolving Final Parting presents all cards from library for first pick (hand)")
    void resolvingPresentsAllCardsForHandPick() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.interaction.librarySearch().playerId()).isEqualTo(player1.getId());
        assertThat(gd.interaction.librarySearch().cards()).hasSize(4);
        assertThat(gd.interaction.librarySearch().destination())
                .isEqualTo(LibrarySearchDestination.HAND);
        assertThat(gd.pendingCardToGraveyardSearch).isTrue();
    }

    @Test
    @DisplayName("First pick puts card into hand, then presents library for second pick (graveyard)")
    void firstPickToHandThenGraveyardSearch() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        int handBefore = gd.playerHands.get(player1.getId()).size();

        // Pick first card for hand
        String chosenName = gd.interaction.librarySearch().cards().getFirst().getName();
        harness.getGameService().handleLibraryCardChosen(gd, player1, 0);

        // Card is in hand
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals(chosenName));

        // Second search begins for graveyard
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.interaction.librarySearch().destination())
                .isEqualTo(LibrarySearchDestination.GRAVEYARD);
        assertThat(gd.pendingCardToGraveyardSearch).isFalse();
        // Library lost one card, so second search shows 3
        assertThat(gd.interaction.librarySearch().cards()).hasSize(3);
    }

    @Test
    @DisplayName("Second pick puts card into graveyard and shuffles library")
    void secondPickToGraveyard() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // First pick: hand
        harness.getGameService().handleLibraryCardChosen(gd, player1, 0);
        // Second pick: graveyard
        String graveyardCardName = gd.interaction.librarySearch().cards().getFirst().getName();
        harness.getGameService().handleLibraryCardChosen(gd, player1, 0);

        // Card is in graveyard (Final Parting itself + the chosen card)
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals(graveyardCardName));

        // Interaction cleared
        assertThat(gd.interaction.awaitingInputType()).isNull();
    }

    @Test
    @DisplayName("Both picks choose different cards correctly")
    void bothPicksChooseDifferentCards() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        int handBefore = gd.playerHands.get(player1.getId()).size();

        // First search: pick Plains for hand
        int plainsIndex = findCardIndex(gd, "Plains");
        harness.getGameService().handleLibraryCardChosen(gd, player1, plainsIndex);

        // Second search: pick Grizzly Bears for graveyard
        int bearsIndex = findCardIndex(gd, "Grizzly Bears");
        harness.getGameService().handleLibraryCardChosen(gd, player1, bearsIndex);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Plains"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    // ===== Unrestricted search — cannot fail to find =====

    @Test
    @DisplayName("First pick is unrestricted (canFailToFind is false)")
    void firstPickCannotFailToFind() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.librarySearch().canFailToFind()).isFalse();
    }

    @Test
    @DisplayName("Second pick is unrestricted (canFailToFind is false)")
    void secondPickCannotFailToFind() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.getGameService().handleLibraryCardChosen(gd, player1, 0);

        assertThat(gd.interaction.librarySearch().canFailToFind()).isFalse();
    }

    // ===== Edge cases =====

    @Test
    @DisplayName("Library with only one card: hand pick offered, no graveyard pick")
    void libraryWithOneCard() {
        setupAndCast();
        List<Card> deck = harness.getGameData().playerDecks.get(player1.getId());
        deck.clear();
        deck.add(new Plains());

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.librarySearch().cards()).hasSize(1);

        // Pick the only card for hand
        harness.getGameService().handleLibraryCardChosen(gd, player1, 0);

        // Library is now empty — no second pick, just finishes
        assertThat(gd.interaction.awaitingInputType()).isNull();
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Plains"));
    }

    @Test
    @DisplayName("Empty library logs and does not crash")
    void emptyLibrary() {
        setupAndCast();
        harness.getGameData().playerDecks.get(player1.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.gameLog).anyMatch(entry -> entry.contains("it is empty"));
    }

    // ===== Sorcery goes to graveyard =====

    @Test
    @DisplayName("Final Parting goes to graveyard after fully resolving")
    void goesToGraveyardAfterResolving() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // First pick: hand
        harness.getGameService().handleLibraryCardChosen(gd, player1, 0);
        // Second pick: graveyard
        harness.getGameService().handleLibraryCardChosen(gd, player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Final Parting"));
    }

    @Test
    @DisplayName("First pick does not reveal (unrestricted search)")
    void firstPickDoesNotReveal() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.librarySearch().reveals()).isFalse();
    }

    // ===== Helpers =====

    private void setupAndCast() {
        harness.setHand(player1, List.of(new FinalParting()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castSorcery(player1, 0, 0);
    }

    private void setupLibrary() {
        List<Card> deck = harness.getGameData().playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new Plains(), new Swamp(), new GrizzlyBears(), new GrizzlyBears()));
    }

    private int findCardIndex(GameData gd, String cardName) {
        var cards = gd.interaction.librarySearch().cards();
        for (int i = 0; i < cards.size(); i++) {
            if (cards.get(i).getName().equals(cardName)) {
                return i;
            }
        }
        throw new IllegalStateException("Card not found: " + cardName);
    }
}
