package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GreenSunsZenithTest extends BaseCardTest {

    // ===== Casting =====

    @Test
    @DisplayName("Casting Green Sun's Zenith with X=2 puts it on the stack with correct X value")
    void castingPutsOnStackWithXValue() {
        harness.setHand(player1, List.of(new GreenSunsZenith()));
        harness.addMana(player1, ManaColor.GREEN, 3); // {X}{G} with X=2 costs 2G+G = 3 mana

        harness.castSorcery(player1, 0, 2);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getCard().getName()).isEqualTo("Green Sun's Zenith");
        assertThat(entry.getXValue()).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    // ===== Resolving — library search =====

    @Test
    @DisplayName("Resolving presents only green creatures with MV <= X")
    void resolvingPresentsOnlyEligibleGreenCreatures() {
        castZenith(2);
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.interaction.librarySearch().playerId()).isEqualTo(player1.getId());
        // Library: LlanowarElves (MV 1, green), GrizzlyBears (MV 2, green), AirElemental (MV 5, blue), Plains, Swamp
        // X=2 → only green creatures with MV <= 2: LlanowarElves (1) and GrizzlyBears (2)
        assertThat(gd.interaction.librarySearch().cards()).hasSize(2);
        assertThat(gd.interaction.librarySearch().cards().stream().map(Card::getName))
                .containsExactlyInAnyOrder("Llanowar Elves", "Grizzly Bears");
    }

    @Test
    @DisplayName("X=1 excludes green creatures with MV > 1")
    void xOneExcludesHigherMVCreatures() {
        castZenith(1);
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
        // Only LlanowarElves (MV 1, green) qualifies
        assertThat(gd.interaction.librarySearch().cards()).hasSize(1);
        assertThat(gd.interaction.librarySearch().cards().getFirst().getName()).isEqualTo("Llanowar Elves");
    }

    @Test
    @DisplayName("Non-green creatures are excluded even if MV matches")
    void nonGreenCreaturesAreExcluded() {
        castZenith(10);
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // AirElemental (MV 5, blue) should not appear despite high X
        assertThat(gd.interaction.librarySearch().cards().stream().map(Card::getName))
                .doesNotContain("Air Elemental");
        // Only green creatures
        assertThat(gd.interaction.librarySearch().cards().stream().map(Card::getName))
                .containsExactlyInAnyOrder("Llanowar Elves", "Grizzly Bears");
    }

    @Test
    @DisplayName("Non-creature cards are excluded even if green")
    void nonCreatureCardsAreExcluded() {
        castZenith(10);
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.librarySearch().cards())
                .allMatch(c -> c.hasType(CardType.CREATURE));
    }

    @Test
    @DisplayName("Multicolor creature with green is included")
    void multicolorCreatureWithGreenIsIncluded() {
        castZenith(3);

        List<Card> deck = harness.getGameData().playerDecks.get(player1.getId());
        deck.clear();
        // GlissaTheTraitor: MV 3, black/green creature — should be eligible
        deck.addAll(List.of(new GlissaTheTraitor(), new AirElemental()));

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.interaction.librarySearch().cards()).hasSize(1);
        assertThat(gd.interaction.librarySearch().cards().getFirst().getName()).isEqualTo("Glissa, the Traitor");
    }

    @Test
    @DisplayName("Search destination is battlefield, not hand")
    void searchDestinationIsBattlefield() {
        castZenith(2);
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.librarySearch().destination())
                .isEqualTo(LibrarySearchDestination.BATTLEFIELD);
    }

    // ===== Choosing a card =====

    @Test
    @DisplayName("Choosing a creature puts it onto the battlefield")
    void choosingCreaturePutsItOntoBattlefield() {
        castZenith(2);
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        String chosenName = gd.interaction.librarySearch().cards().getFirst().getName();

        harness.getGameService().handleLibraryCardChosen(gd, player1, 0);

        // Card is on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals(chosenName));

        // Card is NOT in hand
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getName().equals(chosenName));

        // Library lost one card
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);

        // Awaiting state is cleared
        assertThat(gd.interaction.awaitingInputType()).isNull();
    }

    // ===== Shuffle into library (Zenith mechanic) =====

    @Test
    @DisplayName("Green Sun's Zenith is shuffled into library instead of going to graveyard")
    void zenithShufflesIntoLibraryInsteadOfGraveyard() {
        castZenith(2);
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.getGameService().handleLibraryCardChosen(gd, player1, 0);

        // Zenith should NOT be in graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Green Sun's Zenith"));

        // Zenith should be in library
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Green Sun's Zenith"));
    }

    // ===== Fail to find =====

    @Test
    @DisplayName("Search with stated qualities allows fail to find")
    void canFailToFindIsTrue() {
        castZenith(2);
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.librarySearch().canFailToFind()).isTrue();
    }

    @Test
    @DisplayName("Player can fail to find by choosing index -1")
    void failToFindShufflesLibrary() {
        castZenith(2);
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.getGameService().handleLibraryCardChosen(gd, player1, -1);

        // No card added to hand or battlefield
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);

        // Awaiting state is cleared
        assertThat(gd.interaction.awaitingInputType()).isNull();
    }

    // ===== No eligible creatures =====

    @Test
    @DisplayName("When no green creatures with MV <= X exist, shuffles library and logs")
    void noEligibleCreaturesShufflesAndLogs() {
        castZenith(0);
        setupLibrary(); // Lowest MV green creature is LlanowarElves at MV 1

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.gameLog).anyMatch(entry -> entry.contains("finds no green creature card with mana value"));
    }

    @Test
    @DisplayName("When library has only non-green creatures, shuffles and logs")
    void onlyNonGreenCreaturesInLibrary() {
        castZenith(10);

        List<Card> deck = harness.getGameData().playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new AirElemental(), new Plains()));

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.gameLog).anyMatch(entry -> entry.contains("finds no green creature card with mana value"));
    }

    // ===== Empty library =====

    @Test
    @DisplayName("Resolving with empty library logs and does not crash")
    void emptyLibrary() {
        castZenith(3);

        harness.getGameData().playerDecks.get(player1.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.gameLog).anyMatch(entry -> entry.contains("it is empty"));
    }

    // ===== Helpers =====

    private void castZenith(int xValue) {
        harness.setHand(player1, List.of(new GreenSunsZenith()));
        // {X}{G} costs X generic + 1 green
        harness.addMana(player1, ManaColor.GREEN, xValue + 1);
        harness.castSorcery(player1, 0, xValue);
    }

    private void setupLibrary() {
        List<Card> deck = harness.getGameData().playerDecks.get(player1.getId());
        deck.clear();
        // LlanowarElves: MV 1 (green creature), GrizzlyBears: MV 2 (green creature),
        // AirElemental: MV 5 (blue creature), Plains: MV 0 (basic land), Swamp: MV 0 (basic land)
        deck.addAll(List.of(new LlanowarElves(), new GrizzlyBears(), new AirElemental(), new Plains(), new Swamp()));
    }
}
