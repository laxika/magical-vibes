package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCardTypesToBattlefieldEffect;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RampantGrowthTest {

    private GameTestHarness harness;
    private Player player1;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        harness.skipMulligan();
        harness.clearMessages();
    }

    @Test
    @DisplayName("Rampant Growth has correct card properties")
    void hasCorrectProperties() {
        RampantGrowth card = new RampantGrowth();

        assertThat(card.getName()).isEqualTo("Rampant Growth");
        assertThat(card.getType()).isEqualTo(CardType.SORCERY);
        assertThat(card.getManaCost()).isEqualTo("{1}{G}");
        assertThat(card.getColor()).isEqualTo(CardColor.GREEN);
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst())
                .isInstanceOf(SearchLibraryForCardTypesToBattlefieldEffect.class);
        SearchLibraryForCardTypesToBattlefieldEffect effect =
                (SearchLibraryForCardTypesToBattlefieldEffect) card.getEffects(EffectSlot.SPELL).getFirst();
        assertThat(effect.cardTypes()).containsExactly(CardType.LAND);
        assertThat(effect.requiresBasicSupertype()).isTrue();
        assertThat(effect.entersTapped()).isTrue();
    }

    @Test
    @DisplayName("Casting Rampant Growth puts it on the stack")
    void castingPutsItOnStack() {
        harness.setHand(player1, List.of(new RampantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorcery(player1, 0, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Rampant Growth");
    }

    @Test
    @DisplayName("Resolving Rampant Growth presents only basic lands and destination is battlefield tapped")
    void resolvingPresentsBasicLandsToBattlefieldTapped() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.interaction.awaitingLibrarySearchCards()).hasSize(3);
        assertThat(gd.interaction.awaitingLibrarySearchCards())
                .allMatch(c -> c.getType() == CardType.LAND && c.getSupertypes().contains(CardSupertype.BASIC));
        assertThat(gd.interaction.awaitingLibrarySearchDestination())
                .isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
    }

    @Test
    @DisplayName("Chosen basic land enters battlefield tapped")
    void chosenBasicLandEntersTapped() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        int battlefieldBefore = gd.playerBattlefields.get(player1.getId()).size();
        harness.getGameService().handleLibraryCardChosen(gd, player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore + 1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getType() == CardType.LAND && p.isTapped());
        assertThat(gd.interaction.awaitingInputType()).isNull();
    }

    @Test
    @DisplayName("Player can fail to find with Rampant Growth")
    void canFailToFind() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();
        GameData gd = harness.getGameData();

        harness.getGameService().handleLibraryCardChosen(gd, player1, -1);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getType() == CardType.LAND && p.isTapped());
        assertThat(gd.interaction.awaitingInputType()).isNull();
    }

    @Test
    @DisplayName("Resolving with no basic lands does not prompt for library choice")
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
    @DisplayName("Resolving with empty library does not prompt for library choice")
    void emptyLibraryNoPrompt() {
        setupAndCast();
        harness.getGameData().playerDecks.get(player1.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.gameLog).anyMatch(entry -> entry.contains("it is empty"));
    }

    private void setupAndCast() {
        harness.setHand(player1, List.of(new RampantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castSorcery(player1, 0, 0);
    }

    private void setupLibrary() {
        List<Card> deck = harness.getGameData().playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new Plains(), new Forest(), new Island(), new GrizzlyBears()));
    }
}
