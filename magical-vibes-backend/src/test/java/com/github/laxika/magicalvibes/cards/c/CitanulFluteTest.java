package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCreatureWithMVXOrLessToHandEffect;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CitanulFluteTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        harness.skipMulligan();
        harness.clearMessages();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Citanul Flute has correct card properties")
    void hasCorrectProperties() {
        CitanulFlute card = new CitanulFlute();

        assertThat(card.getName()).isEqualTo("Citanul Flute");
        assertThat(card.getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(card.getManaCost()).isEqualTo("{5}");
        assertThat(card.getActivatedAbilities()).hasSize(1);

        var ability = card.getActivatedAbilities().getFirst();
        assertThat(ability.isRequiresTap()).isTrue();
        assertThat(ability.getManaCost()).isEqualTo("{X}");
        assertThat(ability.isNeedsTarget()).isFalse();
        assertThat(ability.getEffects()).hasSize(1);
        assertThat(ability.getEffects().getFirst())
                .isInstanceOf(SearchLibraryForCreatureWithMVXOrLessToHandEffect.class);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Citanul Flute puts it on the stack")
    void castingPutsItOnStack() {
        harness.setHand(player1, List.of(new CitanulFlute()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castArtifact(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Citanul Flute");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Resolving Citanul Flute puts it on the battlefield")
    void resolvingPutsItOnBattlefield() {
        harness.setHand(player1, List.of(new CitanulFlute()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Citanul Flute"));
    }

    // ===== Activate ability =====

    @Test
    @DisplayName("Activating ability puts it on the stack with correct X value")
    void activatingAbilityPutsOnStack() {
        harness.addToBattlefield(player1, new CitanulFlute());
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, 3, null);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Citanul Flute");
        assertThat(entry.getXValue()).isEqualTo(3);
    }

    @Test
    @DisplayName("Activating ability taps the permanent")
    void activatingAbilityTapsPermanent() {
        harness.addToBattlefield(player1, new CitanulFlute());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, 2, null);

        GameData gd = harness.getGameData();
        Permanent flute = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(flute.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activating ability with X=0 requires no mana")
    void activatingWithXZeroRequiresNoMana() {
        harness.addToBattlefield(player1, new CitanulFlute());
        // No mana added

        harness.activateAbility(player1, 0, 0, null);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getXValue()).isEqualTo(0);
    }

    @Test
    @DisplayName("Mana is consumed when activating ability")
    void manaIsConsumedWhenActivating() {
        harness.addToBattlefield(player1, new CitanulFlute());
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, 3, null);

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Artifact does not have summoning sickness — can activate immediately")
    void noSummoningSicknessForArtifact() {
        harness.addToBattlefield(player1, new CitanulFlute());
        // addToBattlefield sets summoningSick=true, but artifacts ignore it

        harness.activateAbility(player1, 0, 0, null);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
    }

    // ===== Validation errors =====

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        harness.addToBattlefield(player1, new CitanulFlute());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 3, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Cannot activate ability when already tapped")
    void cannotActivateWhenTapped() {
        harness.addToBattlefield(player1, new CitanulFlute());
        GameData gd = harness.getGameData();
        gd.playerBattlefields.get(player1.getId()).getFirst().tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    // ===== Resolving ability — library search =====

    @Test
    @DisplayName("Resolving ability presents only creatures with MV <= X for choice")
    void resolvingPresentsOnlyEligibleCreatures() {
        addFluteAndActivate(2);
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.awaitingInput).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.awaitingLibrarySearchPlayerId).isEqualTo(player1.getId());
        // Library has: LlanowarElves (MV 1), GrizzlyBears (MV 2), AirElemental (MV 5), Plains, Swamp
        // X=2 → only creatures with MV <= 2: LlanowarElves (1) and GrizzlyBears (2)
        assertThat(gd.awaitingLibrarySearchCards).hasSize(2);
        assertThat(gd.awaitingLibrarySearchCards.stream().map(Card::getName))
                .containsExactlyInAnyOrder("Llanowar Elves", "Grizzly Bears");
    }

    @Test
    @DisplayName("X=1 excludes creatures with MV > 1")
    void xOneExcludesHigherMVCreatures() {
        addFluteAndActivate(1);
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.awaitingInput).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
        // Only LlanowarElves (MV 1) qualifies
        assertThat(gd.awaitingLibrarySearchCards).hasSize(1);
        assertThat(gd.awaitingLibrarySearchCards.getFirst().getName()).isEqualTo("Llanowar Elves");
    }

    @Test
    @DisplayName("X=5 includes all creatures in library")
    void xFiveIncludesAllCreatures() {
        addFluteAndActivate(5);
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.awaitingInput).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
        // LlanowarElves (MV 1), GrizzlyBears (MV 2), AirElemental (MV 5) — all qualify
        assertThat(gd.awaitingLibrarySearchCards).hasSize(3);
        assertThat(gd.awaitingLibrarySearchCards.stream().map(Card::getName))
                .containsExactlyInAnyOrder("Llanowar Elves", "Grizzly Bears", "Air Elemental");
    }

    @Test
    @DisplayName("Non-creature cards are never offered even if MV matches")
    void nonCreatureCardsAreExcluded() {
        addFluteAndActivate(10);
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Plains (basic land) and Swamp (basic land) should NOT appear despite high X
        assertThat(gd.awaitingLibrarySearchCards)
                .allMatch(c -> c.getType() == CardType.CREATURE);
    }

    // ===== Choosing a card =====

    @Test
    @DisplayName("Choosing a creature puts it into hand and shuffles library")
    void choosingCreaturePutsItIntoHand() {
        addFluteAndActivate(2);
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        String chosenName = gd.awaitingLibrarySearchCards.getFirst().getName();

        harness.getGameService().handleLibraryCardChosen(gd, player1, 0);

        // Card is in hand
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals(chosenName));

        // Library lost one card
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);

        // Awaiting state is cleared
        assertThat(gd.awaitingInput).isNull();
        assertThat(gd.awaitingLibrarySearchPlayerId).isNull();
        assertThat(gd.awaitingLibrarySearchCards).isNull();
    }

    @Test
    @DisplayName("Chosen card is revealed (oracle text says 'reveal it')")
    void chosenCardIsRevealed() {
        addFluteAndActivate(2);
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.awaitingLibrarySearchReveals).isTrue();

        harness.getGameService().handleLibraryCardChosen(gd, player1, 0);

        // Log should mention "reveals" and "puts it into their hand"
        assertThat(gd.gameLog).anyMatch(entry -> entry.contains("reveals") && entry.contains("puts it into their hand"));
    }

    // ===== Fail to find (CR 701.19b — searching for stated qualities) =====

    @Test
    @DisplayName("Search with stated qualities sets canFailToFind to true")
    void canFailToFindIsTrue() {
        addFluteAndActivate(2);
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.awaitingLibrarySearchCanFailToFind).isTrue();
    }

    @Test
    @DisplayName("Player can fail to find by choosing index -1")
    void failToFindShufflesLibrary() {
        addFluteAndActivate(2);
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.getGameService().handleLibraryCardChosen(gd, player1, -1);

        // No card added to hand
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);

        // Library unchanged in size (just shuffled)
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);

        // Awaiting state is cleared
        assertThat(gd.awaitingInput).isNull();

        // Log mentions declining
        assertThat(gd.gameLog).anyMatch(entry -> entry.contains("chooses not to take a card"));
    }

    // ===== No eligible creatures =====

    @Test
    @DisplayName("When no creatures with MV <= X exist, shuffles library and logs")
    void noEligibleCreaturesShufflesAndLogs() {
        addFluteAndActivate(0);
        setupLibrary(); // Lowest MV creature is LlanowarElves at MV 1

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.awaitingInput).isNotEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.gameLog).anyMatch(entry -> entry.contains("finds no creature card with mana value"));
    }

    @Test
    @DisplayName("When library has only non-creature cards, shuffles and logs")
    void onlyNonCreatureCardsInLibrary() {
        addFluteAndActivate(5);

        List<Card> deck = harness.getGameData().playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new Plains(), new Swamp()));

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.awaitingInput).isNotEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.gameLog).anyMatch(entry -> entry.contains("finds no creature card with mana value"));
    }

    // ===== Empty library =====

    @Test
    @DisplayName("Resolving with empty library logs and does not crash")
    void emptyLibrary() {
        addFluteAndActivate(3);

        harness.getGameData().playerDecks.get(player1.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.awaitingInput).isNotEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.gameLog).anyMatch(entry -> entry.contains("it is empty"));
    }

    // ===== Helpers =====

    private void addFluteAndActivate(int xValue) {
        harness.addToBattlefield(player1, new CitanulFlute());
        harness.addMana(player1, ManaColor.GREEN, xValue);
        harness.activateAbility(player1, 0, xValue, null);
    }

    private void setupLibrary() {
        List<Card> deck = harness.getGameData().playerDecks.get(player1.getId());
        deck.clear();
        // LlanowarElves: MV 1 (creature), GrizzlyBears: MV 2 (creature), AirElemental: MV 5 (creature)
        // Plains: MV 0 (basic land), Swamp: MV 0 (basic land)
        deck.addAll(List.of(new LlanowarElves(), new GrizzlyBears(), new AirElemental(), new Plains(), new Swamp()));
    }
}
