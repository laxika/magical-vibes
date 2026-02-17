package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FieldMarshal;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForBasicLandToHandEffect;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ChangelingWayfinderTest {

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
    @DisplayName("Changeling Wayfinder has correct card properties")
    void hasCorrectProperties() {
        ChangelingWayfinder card = new ChangelingWayfinder();

        assertThat(card.getName()).isEqualTo("Changeling Wayfinder");
        assertThat(card.getType()).isEqualTo(CardType.CREATURE);
        assertThat(card.getManaCost()).isEqualTo("{3}");
        assertThat(card.getColor()).isNull();
        assertThat(card.getPower()).isEqualTo(1);
        assertThat(card.getToughness()).isEqualTo(2);
        assertThat(card.getSubtypes()).containsExactly(CardSubtype.SHAPESHIFTER);
        assertThat(card.getKeywords()).isEqualTo(Set.of(Keyword.CHANGELING));
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(MayEffect.class);
        MayEffect mayEffect = (MayEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(mayEffect.wrapped()).isInstanceOf(SearchLibraryForBasicLandToHandEffect.class);
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting Changeling Wayfinder puts it on the stack")
    void castingPutsItOnStack() {
        harness.setHand(player1, List.of(new ChangelingWayfinder()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Changeling Wayfinder");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Resolving Changeling Wayfinder puts it on the battlefield with may prompt")
    void resolvingPutsItOnBattlefieldWithMayPrompt() {
        setupAndCast();
        harness.passBothPriorities(); // resolve creature spell → may prompt

        GameData gd = harness.getGameData();

        // Changeling Wayfinder is on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Changeling Wayfinder"));

        // May ability prompt is pending
        assertThat(gd.awaitingInput).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
        assertThat(gd.awaitingMayAbilityPlayerId).isEqualTo(player1.getId());
    }

    // ===== ETB: Accept and search =====

    @Test
    @DisplayName("Accepting may ability puts ETB triggered ability on stack")
    void acceptingMayPutsEtbOnStack() {
        setupAndCast();
        harness.passBothPriorities(); // resolve creature spell → may prompt
        harness.handleMayAbilityChosen(player1, true); // accept → ETB on stack

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry etbEntry = gd.stack.getFirst();
        assertThat(etbEntry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(etbEntry.getCard().getName()).isEqualTo("Changeling Wayfinder");
    }

    @Test
    @DisplayName("ETB resolves and presents basic lands from library for choice")
    void etbPresentsBasicLandsForChoice() {
        setupAndCast();
        setupLibraryWithBasicLands();

        harness.passBothPriorities(); // resolve creature spell → may prompt
        harness.handleMayAbilityChosen(player1, true); // accept → ETB on stack
        harness.passBothPriorities(); // resolve ETB → library search prompt

        GameData gd = harness.getGameData();
        assertThat(gd.awaitingInput).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.awaitingLibrarySearchPlayerId).isEqualTo(player1.getId());
        assertThat(gd.awaitingLibrarySearchCards).hasSize(3);
        assertThat(gd.awaitingLibrarySearchCards.stream().map(Card::getName))
                .containsExactlyInAnyOrder("Plains", "Forest", "Island");
    }

    @Test
    @DisplayName("Choosing a basic land puts it into hand and shuffles library")
    void choosingBasicLandPutsItIntoHand() {
        setupAndCast();
        setupLibraryWithBasicLands();

        harness.passBothPriorities(); // resolve creature spell → may prompt
        harness.handleMayAbilityChosen(player1, true); // accept → ETB on stack
        harness.passBothPriorities(); // resolve ETB → library search prompt

        GameData gd = harness.getGameData();
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        // Choose the first basic land (index 0)
        harness.getGameService().handleLibraryCardChosen(gd, player1, 0);

        // Card is in hand
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getType() == CardType.LAND && c.getSupertypes().contains(CardSupertype.BASIC));

        // Library lost one card
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);

        // Awaiting state is cleared
        assertThat(gd.awaitingInput).isNull();
        assertThat(gd.awaitingLibrarySearchPlayerId).isNull();
        assertThat(gd.awaitingLibrarySearchCards).isNull();

        // Log mentions the reveal
        assertThat(gd.gameLog).anyMatch(entry -> entry.contains("reveals") && entry.contains("puts it into their hand"));
    }

    @Test
    @DisplayName("Can choose a specific basic land when multiple are available")
    void canChooseSpecificBasicLand() {
        setupAndCast();
        setupLibraryWithBasicLands();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        List<Card> searchCards = gd.awaitingLibrarySearchCards;
        String chosenName = searchCards.get(2).getName(); // pick the third card

        harness.getGameService().handleLibraryCardChosen(gd, player1, 2);

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals(chosenName));
    }

    // ===== ETB: Fail to find (decline to take a card) =====

    @Test
    @DisplayName("Player can fail to find by choosing index -1")
    void failToFindShufflesLibrary() {
        setupAndCast();
        setupLibraryWithBasicLands();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.getGameService().handleLibraryCardChosen(gd, player1, -1);

        // No card added to hand
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        // Library unchanged in size (just shuffled)
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);

        // Awaiting state is cleared
        assertThat(gd.awaitingInput).isNull();

        // Log mentions declining
        assertThat(gd.gameLog).anyMatch(entry -> entry.contains("chooses not to take a card"));
    }

    // ===== ETB: Decline may ability =====

    @Test
    @DisplayName("Declining may ability does not search library")
    void decliningMayAbilitySkipsSearch() {
        setupAndCast();
        setupLibraryWithBasicLands();

        harness.passBothPriorities(); // resolve creature spell → may prompt
        harness.handleMayAbilityChosen(player1, false); // decline

        GameData gd = harness.getGameData();
        assertThat(gd.awaitingInput).isNotEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.gameLog).noneMatch(entry -> entry.contains("searches their library"));
    }

    // ===== ETB: No basic lands in library =====

    @Test
    @DisplayName("ETB with no basic lands in library shuffles and logs")
    void noBasicLandsInLibrary() {
        setupAndCast();

        // Library has only non-basic-land cards
        List<Card> deck = harness.getGameData().playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new GrizzlyBears(), new GrizzlyBears()));

        harness.passBothPriorities(); // resolve creature spell → may prompt
        harness.handleMayAbilityChosen(player1, true); // accept → ETB on stack
        harness.passBothPriorities(); // resolve ETB → no basic lands

        GameData gd = harness.getGameData();
        assertThat(gd.awaitingInput).isNotEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.gameLog).anyMatch(entry -> entry.contains("finds no basic land cards"));
    }

    @Test
    @DisplayName("ETB with empty library logs and does not crash")
    void emptyLibrary() {
        setupAndCast();

        // Clear the library
        harness.getGameData().playerDecks.get(player1.getId()).clear();

        harness.passBothPriorities(); // resolve creature spell → may prompt
        harness.handleMayAbilityChosen(player1, true); // accept → ETB on stack
        harness.passBothPriorities(); // resolve ETB → empty library

        GameData gd = harness.getGameData();
        assertThat(gd.awaitingInput).isNotEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.gameLog).anyMatch(entry -> entry.contains("it is empty"));
    }

    // ===== Only basic lands are offered =====

    @Test
    @DisplayName("Only basic land cards are offered, non-lands are excluded")
    void onlyBasicLandsOffered() {
        setupAndCast();

        List<Card> deck = harness.getGameData().playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new GrizzlyBears(), new Plains(), new GrizzlyBears(), new Mountain()));

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.awaitingInput).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.awaitingLibrarySearchCards).hasSize(2);
        assertThat(gd.awaitingLibrarySearchCards).allMatch(c -> c.getType() == CardType.LAND && c.getSupertypes().contains(CardSupertype.BASIC));
    }

    // ===== Changeling keyword: counts as every creature type =====

    @Test
    @DisplayName("Changeling gets boost from Field Marshal (Soldier lord) due to being every creature type")
    void changelingGetsSubtypeBoost() {
        harness.addToBattlefield(player1, new FieldMarshal());
        harness.addToBattlefield(player1, new ChangelingWayfinder());

        GameData gd = harness.getGameData();
        Permanent wayfinder = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Changeling Wayfinder"))
                .findFirst()
                .orElseThrow();

        // Field Marshal gives other Soldiers +1/+1 and first strike
        // Changeling Wayfinder is 1/2 base → should be 2/3
        assertThat(harness.getGameService().getEffectivePower(gd, wayfinder)).isEqualTo(2);
        assertThat(harness.getGameService().getEffectiveToughness(gd, wayfinder)).isEqualTo(3);
        assertThat(harness.getGameService().hasKeyword(gd, wayfinder, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Changeling inherently has the CHANGELING keyword")
    void hasChangelingKeyword() {
        harness.addToBattlefield(player1, new ChangelingWayfinder());

        GameData gd = harness.getGameData();
        Permanent wayfinder = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Changeling Wayfinder"))
                .findFirst()
                .orElseThrow();

        assertThat(wayfinder.hasKeyword(Keyword.CHANGELING)).isTrue();
    }

    // ===== Helpers =====

    private void setupAndCast() {
        harness.setHand(player1, List.of(new ChangelingWayfinder()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castCreature(player1, 0);
    }

    private void setupLibraryWithBasicLands() {
        List<Card> deck = harness.getGameData().playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new Plains(), new Forest(), new Island(), new GrizzlyBears(), new GrizzlyBears()));
    }
}
