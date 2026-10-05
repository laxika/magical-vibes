package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.m.MoriokReaver;
import com.github.laxika.magicalvibes.cards.t.TurnAside;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Memoricide.class, MoriokReaver.class, TurnAside.class})
class MemoricideTest extends BaseCardTest {

    @Test
    @DisplayName("Casting puts it on the stack targeting a player")
    void castingPutsItOnStack() {
        harness.setHand(player1, List.of(new Memoricide()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Memoricide");
        assertThat(entry.getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.setHand(player1, List.of(new Memoricide()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Resolving prompts caster for a card name choice")
    void resolvingPromptsForCardNameChoice() {
        harness.setHand(player1, List.of(new Memoricide()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
    }

    @Test
    @DisplayName("After name choice with matches, prompts for card selection")
    void afterNameChoicePromptsForCardSelection() {
        Card bears = new MoriokReaver();
        harness.setHand(player2, new ArrayList<>(List.of(bears)));

        harness.setHand(player1, List.of(new Memoricide()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.handleListChoice(player1, "Moriok Reaver");

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiZoneExileChoice.class);
    }

    @Test
    @DisplayName("Exiles matching cards from target player's hand")
    void exilesMatchingCardsFromHand() {
        Card bears1 = new MoriokReaver();
        Card peek = new TurnAside();
        harness.setHand(player2, new ArrayList<>(List.of(bears1, peek)));

        harness.setHand(player1, List.of(new Memoricide()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        // Choose "Moriok Reaver"
        harness.handleListChoice(player1, "Moriok Reaver");

        // Select all matching cards to exile
        harness.handleMultipleCardsChosen(player1, List.of(bears1.getId()));

        // Moriok Reaver should be exiled
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Moriok Reaver"));

        // Moriok Reaver should not be in hand
        harness.assertNotInHand(player2, "Moriok Reaver");

        // TurnAside should remain in hand
        harness.assertInHand(player2, "Turn Aside");
    }

    @Test
    @DisplayName("Exiles matching cards from target player's graveyard")
    void exilesMatchingCardsFromGraveyard() {
        Card bears = new MoriokReaver();
        harness.setGraveyard(player2, new ArrayList<>(List.of(bears)));
        harness.setHand(player2, List.of());

        harness.setHand(player1, List.of(new Memoricide()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.handleListChoice(player1, "Moriok Reaver");
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Moriok Reaver"));
        harness.assertNotInGraveyard(player2, "Moriok Reaver");
    }

    @Test
    @DisplayName("Exiles matching cards from target player's library")
    void exilesMatchingCardsFromLibrary() {
        Card bears = new MoriokReaver();
        harness.setLibrary(player2, List.of(bears));
        harness.setHand(player2, List.of());

        harness.setHand(player1, List.of(new Memoricide()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.handleListChoice(player1, "Moriok Reaver");
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Moriok Reaver"));
        assertThat(gd.playerDecks.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Moriok Reaver"));
    }

    @Test
    @DisplayName("Exiles matching cards from all three zones at once")
    void exilesMatchingCardsFromAllZones() {
        Card bears1 = new MoriokReaver();
        Card bears2 = new MoriokReaver();
        Card bears3 = new MoriokReaver();
        Card peek = new TurnAside();

        harness.setHand(player2, new ArrayList<>(List.of(bears1, peek)));
        harness.setGraveyard(player2, new ArrayList<>(List.of(bears2)));
        harness.setLibrary(player2, List.of(bears3));

        harness.setHand(player1, List.of(new Memoricide()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.handleListChoice(player1, "Moriok Reaver");
        harness.handleMultipleCardsChosen(player1, List.of(bears1.getId(), bears2.getId(), bears3.getId()));

        // All 3 copies should be exiled
        long exiledCount = gd.getPlayerExiledCards(player2.getId()).stream()
                .filter(c -> c.getName().equals("Moriok Reaver"))
                .count();
        assertThat(exiledCount).isEqualTo(3);

        // No Moriok Reaver in any zone
        harness.assertNotInHand(player2, "Moriok Reaver");
        harness.assertNotInGraveyard(player2, "Moriok Reaver");
        assertThat(gd.playerDecks.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Moriok Reaver"));

        // TurnAside should remain in hand
        harness.assertInHand(player2, "Turn Aside");
    }

    @Test
    @DisplayName("Choosing a name with no matching cards just shuffles library")
    void noMatchingCardsResultsInNoExile() {
        Card peek = new TurnAside();
        harness.setHand(player2, new ArrayList<>(List.of(peek)));
        harness.setGraveyard(player2, List.of());
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        harness.setHand(player1, List.of(new Memoricide()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.handleListChoice(player1, "Moriok Reaver");

        // No card selection step â€” resolves immediately when no matches
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiZoneExileChoice.class)).isNull();

        // No cards exiled (no Moriok Reaver in any zone)
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(c -> c.getName().equals("Moriok Reaver"));

        // Hand unchanged
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);

        // Library size unchanged (just shuffled)
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore);

        // Log should mention 0 cards exiled
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("exiles 0 cards"));
    }

    @Test
    @DisplayName("Library is shuffled after resolution")
    void libraryIsShuffledAfterResolution() {
        // Fill library with predictable cards
        List<Card> originalOrder = new ArrayList<>();
        List<UUID> bearsIds = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            Card bears = new MoriokReaver();
            originalOrder.add(bears);
            bearsIds.add(bears.getId());
        }
        originalOrder.add(new TurnAside());
        harness.setLibrary(player2, originalOrder);
        harness.setHand(player2, List.of());

        harness.setHand(player1, List.of(new Memoricide()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.handleListChoice(player1, "Moriok Reaver");
        harness.handleMultipleCardsChosen(player1, bearsIds);

        // All Moriok Reaver exiled, only TurnAside should remain
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId()).getFirst().getName()).isEqualTo("Turn Aside");

        // Log should mention shuffle
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("shuffles their library"));
    }

    @Test
    @DisplayName("Partial selection: only selected cards are exiled")
    void partialSelectionOnlyExilesSelected() {
        Card bears1 = new MoriokReaver();
        Card bears2 = new MoriokReaver();
        Card bears3 = new MoriokReaver();

        harness.setHand(player2, new ArrayList<>(List.of(bears1)));
        harness.setGraveyard(player2, new ArrayList<>(List.of(bears2)));
        harness.setLibrary(player2, List.of(bears3));

        harness.setHand(player1, List.of(new Memoricide()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.handleListChoice(player1, "Moriok Reaver");

        // Only select the one from hand â€” leave graveyard and library copies
        harness.handleMultipleCardsChosen(player1, List.of(bears1.getId()));

        // Only 1 card exiled
        long exiledCount = gd.getPlayerExiledCards(player2.getId()).stream()
                .filter(c -> c.getName().equals("Moriok Reaver"))
                .count();
        assertThat(exiledCount).isEqualTo(1);

        // Hand copy gone
        harness.assertNotInHand(player2, "Moriok Reaver");

        // Graveyard copy still there
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getId().equals(bears2.getId()));

        // Library copy still there
        assertThat(gd.playerDecks.get(player2.getId()))
                .anyMatch(c -> c.getId().equals(bears3.getId()));

        // Library still shuffled
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("shuffles their library"));
    }

    @Test
    @DisplayName("Zero selection: no cards exiled but library is shuffled")
    void zeroSelectionNoCardsExiled() {
        Card bears1 = new MoriokReaver();
        Card bears2 = new MoriokReaver();

        harness.setHand(player2, new ArrayList<>(List.of(bears1)));
        harness.setGraveyard(player2, new ArrayList<>(List.of(bears2)));

        harness.setHand(player1, List.of(new Memoricide()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.handleListChoice(player1, "Moriok Reaver");

        // Select zero cards
        harness.handleMultipleCardsChosen(player1, List.of());

        // No cards exiled
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(c -> c.getName().equals("Moriok Reaver"));

        // Both copies remain in their zones
        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(c -> c.getId().equals(bears1.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getId().equals(bears2.getId()));

        // Log should mention 0 cards exiled and library shuffled
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("exiles 0 cards"));
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("shuffles their library"));
    }

    @Test
    @DisplayName("Can target self")
    void canTargetSelf() {
        Card bears = new MoriokReaver();
        harness.setHand(player1, new ArrayList<>(List.of(new Memoricide(), bears)));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "Moriok Reaver");
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        // Moriok Reaver exiled from player1's hand
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Moriok Reaver"));
        harness.assertNotInHand(player1, "Moriok Reaver");
    }

    @Test
    @DisplayName("Memoricide goes to caster's graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new Memoricide()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        // No matching cards â€” resolves immediately without selection step
        harness.handleListChoice(player1, "Moriok Reaver");

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Memoricide");
    }

    @Test
    @DisplayName("Name choice is logged")
    void nameChoiceIsLogged() {
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new Memoricide()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.handleListChoice(player1, "Moriok Reaver");

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("chooses") && log.contains("Moriok Reaver"));
    }

    @Test
    @DisplayName("Exile count is logged")
    void exileCountIsLogged() {
        Card bears = new MoriokReaver();
        harness.setHand(player2, new ArrayList<>(List.of(bears)));

        harness.setHand(player1, List.of(new Memoricide()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.handleListChoice(player1, "Moriok Reaver");
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("exiles 1 card"));
    }

    @Test
    @DisplayName("Cannot choose a land card name")
    void cannotChooseLandName() {
        harness.setHand(player1, List.of(new Memoricide()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThatThrownBy(() -> harness.handleListChoice(player1, "Plains"))
                .isInstanceOf(IllegalArgumentException.class);
        harness.handleListChoice(player1, "Memoricide");
        harness.assertInGraveyard(player1, "Memoricide");
    }

    @Test
    @DisplayName("Only target player's searched zones are affected")
    void leavesOtherPlayersCardsAndBattlefieldUntouched() {
        Card ownCopy = new MoriokReaver();
        Card targetCopy = new MoriokReaver();
        harness.setHand(player1, List.of(new Memoricide(), ownCopy));
        harness.setHand(player2, List.of(targetCopy));
        harness.addToBattlefield(player2, new MoriokReaver());
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleListChoice(player1, "Moriok Reaver");
        harness.handleMultipleCardsChosen(player1, List.of(targetCopy.getId()));

        harness.assertInHand(player1, "Moriok Reaver");
        harness.assertOnBattlefield(player2, "Moriok Reaver");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(targetCopy);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(ownCopy);
    }

    @Test
    @DisplayName("Search lets caster inspect nonmatching cards in the hand and library")
    void searchShowsNonmatchingHiddenCards() {
        Card matching = new MoriokReaver();
        harness.setHand(player2, List.of(matching, new TurnAside()));
        harness.setLibrary(player2, List.of(new TurnAside()));
        harness.setHand(player1, List.of(new Memoricide()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.clearMessages();
        harness.handleListChoice(player1, "Moriok Reaver");

        assertThat(harness.getConn1().getSentMessages())
                .anyMatch(message -> message.contains("Turn Aside"));
        assertThat(harness.getConn2().getSentMessages())
                .noneMatch(message -> message.contains("Choose any number") && message.contains("Turn Aside"));
        harness.handleMultipleCardsChosen(player1, List.of(matching.getId()));
    }
}
