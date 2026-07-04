package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCardsToHandEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class IncreasingAmbitionTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Increasing Ambition has a search effect and flashback {7}{B}")
    void hasCorrectProperties() {
        IncreasingAmbition card = new IncreasingAmbition();

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst())
                .isInstanceOf(SearchLibraryForCardsToHandEffect.class);
        SearchLibraryForCardsToHandEffect effect =
                (SearchLibraryForCardsToHandEffect) card.getEffects(EffectSlot.SPELL).getFirst();
        assertThat(effect.count()).isEqualTo(1);
        assertThat(effect.castFromGraveyardCount()).isEqualTo(2);

        FlashbackCast flashback = card.getCastingOption(FlashbackCast.class).orElseThrow();
        assertThat(flashback.getCost(ManaCastingCost.class).orElseThrow().manaCost()).isEqualTo("{7}{B}");
    }

    // ===== Normal cast: search for one card =====

    @Test
    @DisplayName("Cast from hand searches the library for one card")
    void normalCastSearchesForOneCard() {
        harness.setHand(player1, List.of(new IncreasingAmbition()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castSorcery(player1, 0, 0);
        setupLibrary();

        harness.passBothPriorities(); // resolve sorcery -> library search prompt

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().remainingCount()).isEqualTo(1);

        harness.getGameService().handleLibraryCardChosen(gd, player1, 0);

        // Exactly one card searched into hand, no further search pending
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    // ===== Flashback cast: search for two cards =====

    @Test
    @DisplayName("Cast from graveyard via flashback searches the library for two cards")
    void flashbackCastSearchesForTwoCards() {
        harness.setGraveyard(player1, List.of(new IncreasingAmbition()));
        harness.addMana(player1, ManaColor.BLACK, 8); // pays {7}{B}
        harness.castFlashback(player1, 0);
        setupLibrary();

        GameData gd = harness.getGameData();
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.passBothPriorities(); // resolve flashback sorcery -> library search prompt

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().remainingCount()).isEqualTo(2);

        // First pick
        harness.getGameService().handleLibraryCardChosen(gd, player1, 0);

        // A second pick is prompted
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().remainingCount()).isEqualTo(1);

        // Second pick
        harness.getGameService().handleLibraryCardChosen(gd, player1, 0);

        // Two cards searched into hand, search complete
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Flashback exiles Increasing Ambition after resolving")
    void flashbackExilesAfterResolving() {
        harness.setGraveyard(player1, List.of(new IncreasingAmbition()));
        harness.addMana(player1, ManaColor.BLACK, 8);
        harness.castFlashback(player1, 0);
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.getGameService().handleLibraryCardChosen(gd, player1, 0);
        harness.getGameService().handleLibraryCardChosen(gd, player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Increasing Ambition"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Increasing Ambition"));
    }

    // ===== Empty library =====

    @Test
    @DisplayName("Resolving with an empty library logs and does not crash")
    void emptyLibrary() {
        harness.setHand(player1, List.of(new IncreasingAmbition()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castSorcery(player1, 0, 0);
        harness.getGameData().playerDecks.get(player1.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.gameLog).anyMatch(entry -> entry.contains("it is empty"));
    }

    // ===== Helpers =====

    private void setupLibrary() {
        List<Card> deck = harness.getGameData().playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new Plains(), new Swamp(), new GrizzlyBears(), new GrizzlyBears()));
    }
}
