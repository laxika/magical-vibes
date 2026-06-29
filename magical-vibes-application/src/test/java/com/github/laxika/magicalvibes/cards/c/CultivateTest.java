package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForBasicLandsToBattlefieldTappedAndHandEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CultivateTest extends BaseCardTest {

    @Test
    @DisplayName("Cultivate has SearchLibraryForBasicLandsToBattlefieldTappedAndHandEffect as spell effect")
    void hasCorrectEffect() {
        Cultivate card = new Cultivate();

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst()).isInstanceOf(SearchLibraryForBasicLandsToBattlefieldTappedAndHandEffect.class);
    }

    @Test
    @DisplayName("Casting Cultivate puts it on the stack as a sorcery")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new Cultivate()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castSorcery(player1, 0, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Cultivate");
    }

    @Test
    @DisplayName("Resolving Cultivate presents basic lands for battlefield tapped pick")
    void resolvingPresentsBasicLandsForBattlefieldTapped() {
        setupAndCast();
        setupLibraryWithMultipleBasicLands();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.interaction.librarySearch().cards()).hasSize(3);
        assertThat(gd.interaction.librarySearch().cards())
                .allMatch(c -> c.hasType(CardType.LAND) && c.getSupertypes().contains(CardSupertype.BASIC));
        assertThat(gd.interaction.librarySearch().destination())
                .isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
        assertThat(gd.pendingBasicLandToHandSearch).isTrue();
    }

    @Test
    @DisplayName("Picking first card puts it onto battlefield tapped, then presents hand pick")
    void firstPickToBattlefieldThenHandSearch() {
        setupAndCast();
        setupLibraryWithMultipleBasicLands();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        int battlefieldBefore = gd.playerBattlefields.get(player1.getId()).size();

        // Pick first basic land for battlefield tapped
        harness.getGameService().handleLibraryCardChosen(gd, player1, 0);

        // First card enters battlefield tapped
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore + 1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().hasType(CardType.LAND) && p.isTapped());

        // Second search begins for hand
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.interaction.librarySearch().destination())
                .isEqualTo(LibrarySearchDestination.HAND);
        assertThat(gd.pendingBasicLandToHandSearch).isFalse();
    }

    @Test
    @DisplayName("Picking second card puts it into hand and shuffles library")
    void secondPickToHand() {
        setupAndCast();
        setupLibraryWithMultipleBasicLands();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        int handBefore = gd.playerHands.get(player1.getId()).size();

        // First pick: battlefield tapped
        harness.getGameService().handleLibraryCardChosen(gd, player1, 0);
        // Second pick: hand
        harness.getGameService().handleLibraryCardChosen(gd, player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.hasType(CardType.LAND) && c.getSupertypes().contains(CardSupertype.BASIC));
        assertThat(gd.interaction.awaitingInputType()).isNull();
    }

    @Test
    @DisplayName("Failing to find on battlefield pick ends the spell entirely (per ruling)")
    void failToFindOnBattlefieldPickEndsSpell() {
        setupAndCast();
        setupLibraryWithMultipleBasicLands();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int battlefieldBefore = gd.playerBattlefields.get(player1.getId()).size();

        // Decline first pick — per ruling, finding only one card = BF tapped,
        // so declining the BF pick means finding zero. No hand pick offered.
        harness.getGameService().handleLibraryCardChosen(gd, player1, -1);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.interaction.awaitingInputType()).isNull();
        assertThat(gd.pendingBasicLandToHandSearch).isFalse();
    }

    @Test
    @DisplayName("With only one basic land, picking it for battlefield leaves no hand pick")
    void oneBasicLandPickedForBattlefield() {
        setupAndCast();
        List<Card> deck = harness.getGameData().playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new Forest(), new GrizzlyBears(), new GrizzlyBears()));

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Pick the only basic land for battlefield
        harness.getGameService().handleLibraryCardChosen(gd, player1, 0);

        // No more basic lands for hand search — should finish
        assertThat(gd.interaction.awaitingInputType()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().hasType(CardType.LAND) && p.isTapped());
    }

    @Test
    @DisplayName("With only one basic land, skipping battlefield pick ends the spell (per ruling)")
    void oneBasicLandSkipBattlefieldPickEndsSpell() {
        setupAndCast();
        List<Card> deck = harness.getGameData().playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new Forest(), new GrizzlyBears(), new GrizzlyBears()));

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int battlefieldBefore = gd.playerBattlefields.get(player1.getId()).size();

        // Decline battlefield pick — per ruling, if you find only one it must go
        // to the battlefield tapped, so declining means finding zero.
        harness.getGameService().handleLibraryCardChosen(gd, player1, -1);

        // Spell finishes — no hand pick offered
        assertThat(gd.interaction.awaitingInputType()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("Picking battlefield land then declining hand pick is valid (find one)")
    void pickBattlefieldThenDeclineHand() {
        setupAndCast();
        setupLibraryWithMultipleBasicLands();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int battlefieldBefore = gd.playerBattlefields.get(player1.getId()).size();

        // Pick for battlefield
        harness.getGameService().handleLibraryCardChosen(gd, player1, 0);
        // Decline hand pick
        harness.getGameService().handleLibraryCardChosen(gd, player1, -1);

        // One land on battlefield tapped, no new cards in hand
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore + 1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().hasType(CardType.LAND) && p.isTapped());
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.interaction.awaitingInputType()).isNull();
    }

    @Test
    @DisplayName("No basic lands in library logs and shuffles without prompting")
    void noBasicLandsNoPrompt() {
        setupAndCast();
        List<Card> deck = harness.getGameData().playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new GrizzlyBears(), new GrizzlyBears()));

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.gameLog).anyMatch(entry -> entry.contains("finds no basic land cards"));
    }

    @Test
    @DisplayName("Empty library logs without prompting")
    void emptyLibraryNoPrompt() {
        setupAndCast();
        harness.getGameData().playerDecks.get(player1.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.gameLog).anyMatch(entry -> entry.contains("it is empty"));
    }

    private void setupAndCast() {
        harness.setHand(player1, List.of(new Cultivate()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castSorcery(player1, 0, 0);
    }

    private void setupLibraryWithMultipleBasicLands() {
        List<Card> deck = harness.getGameData().playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new Plains(), new Forest(), new Island(), new GrizzlyBears()));
    }
}
