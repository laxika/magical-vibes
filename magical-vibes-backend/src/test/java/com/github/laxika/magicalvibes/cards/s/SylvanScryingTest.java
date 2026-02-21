package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCardTypesToHandEffect;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class SylvanScryingTest {

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
    @DisplayName("Sylvan Scrying has correct card properties")
    void hasCorrectProperties() {
        SylvanScrying card = new SylvanScrying();

        assertThat(card.getName()).isEqualTo("Sylvan Scrying");
        assertThat(card.getType()).isEqualTo(CardType.SORCERY);
        assertThat(card.getManaCost()).isEqualTo("{1}{G}");
        assertThat(card.getColor()).isEqualTo(CardColor.GREEN);
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst())
                .isInstanceOf(SearchLibraryForCardTypesToHandEffect.class);
        SearchLibraryForCardTypesToHandEffect effect =
                (SearchLibraryForCardTypesToHandEffect) card.getEffects(EffectSlot.SPELL).getFirst();
        assertThat(effect.cardTypes()).isEqualTo(Set.of(CardType.LAND));
    }

    @Test
    @DisplayName("Casting Sylvan Scrying puts it on the stack")
    void castingPutsItOnStack() {
        harness.setHand(player1, List.of(new SylvanScrying()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorcery(player1, 0, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Sylvan Scrying");
    }

    @Test
    @DisplayName("Resolving Sylvan Scrying presents only land cards for choice")
    void resolvingPresentsOnlyLands() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.interaction.awaitingLibrarySearchCards()).hasSize(2);
        assertThat(gd.interaction.awaitingLibrarySearchCards())
                .allMatch(c -> c.getType() == CardType.LAND || c.getAdditionalTypes().contains(CardType.LAND));
        assertThat(gd.interaction.awaitingLibrarySearchReveals()).isTrue();
        assertThat(gd.interaction.awaitingLibrarySearchCanFailToFind()).isTrue();
    }

    @Test
    @DisplayName("Choosing a land card puts it into hand")
    void choosingLandPutsItIntoHand() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.getGameService().handleLibraryCardChosen(gd, player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getType() == CardType.LAND || c.getAdditionalTypes().contains(CardType.LAND));
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
    }

    @Test
    @DisplayName("Player can fail to find with Sylvan Scrying")
    void canFailToFind() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.getGameService().handleLibraryCardChosen(gd, player1, -1);

        assertThat(gd.interaction.awaitingInputType()).isNull();
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Plains") || c.getName().equals("Forest"));
    }

    @Test
    @DisplayName("Resolving with no land cards does not prompt for choice")
    void noLandsNoPrompt() {
        setupAndCast();
        List<Card> deck = harness.getGameData().playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new GrizzlyBears(), new GrizzlyBears()));

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.gameLog).anyMatch(entry -> entry.contains("finds no land cards"));
    }

    @Test
    @DisplayName("Resolving with empty library does not prompt for choice")
    void emptyLibraryNoPrompt() {
        setupAndCast();
        harness.getGameData().playerDecks.get(player1.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.gameLog).anyMatch(entry -> entry.contains("it is empty"));
    }

    @Test
    @DisplayName("Sylvan Scrying goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();
        harness.getGameService().handleLibraryCardChosen(harness.getGameData(), player1, 0);

        assertThat(harness.getGameData().playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Sylvan Scrying"));
    }

    private void setupAndCast() {
        harness.setHand(player1, List.of(new SylvanScrying()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castSorcery(player1, 0, 0);
    }

    private void setupLibrary() {
        List<Card> deck = harness.getGameData().playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new Plains(), new Forest(), new GrizzlyBears()));
    }
}
