package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.SearchTargetLibraryForCardToExileWithPlayPermissionEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PraetorsGraspTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has search-exile-with-play-permission effect and targets opponent")
    void hasCorrectEffects() {
        PraetorsGrasp card = new PraetorsGrasp();

        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0)).isInstanceOf(SearchTargetLibraryForCardToExileWithPlayPermissionEffect.class);
    }

    // ===== Library search =====

    @Test
    @DisplayName("Presents library search showing all cards in opponent's library")
    void presentsLibrarySearchWithAllCards() {
        Card bears = new GrizzlyBears();
        Card shock = new Shock();
        Card swamp = new Swamp();
        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).addAll(List.of(bears, shock, swamp));

        harness.setHand(player1, List.of(new PraetorsGrasp()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.interaction.librarySearch().playerId()).isEqualTo(player1.getId());
        // All cards should be shown, not filtered by type
        assertThat(gd.interaction.librarySearch().cards()).hasSize(3);
    }

    // ===== Exile with play permission =====

    @Test
    @DisplayName("Chosen card is exiled under caster's exile zone with play permission")
    void chosenCardIsExiledWithPlayPermission() {
        Card bears = new GrizzlyBears();
        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).add(bears);

        harness.setHand(player1, List.of(new PraetorsGrasp()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Choose the card
        gs.handleLibraryCardChosen(gd, player1, 0);

        // Card should be in caster's exile zone
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getId().equals(bears.getId()));

        // Play permission should be granted to caster
        assertThat(gd.exilePlayPermissions.get(bears.getId()))
                .isEqualTo(player1.getId());

        // Card should not be in opponent's library
        assertThat(gd.playerDecks.get(player2.getId()))
                .noneMatch(c -> c.getId().equals(bears.getId()));
    }

    @Test
    @DisplayName("Opponent's library is shuffled after search")
    void libraryIsShuffledAfterSearch() {
        Card bears = new GrizzlyBears();
        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).add(bears);

        harness.setHand(player1, List.of(new PraetorsGrasp()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        gs.handleLibraryCardChosen(gd, player1, 0);

        // Log should mention shuffle
        assertThat(gd.gameLog).anyMatch(log -> log.contains("shuffled") || log.contains("Library is shuffled"));
    }

    // ===== Empty library =====

    @Test
    @DisplayName("Empty opponent library skips search")
    void emptyLibrarySkipsSearch() {
        gd.playerDecks.get(player2.getId()).clear();

        harness.setHand(player1, List.of(new PraetorsGrasp()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.LIBRARY_SEARCH);
    }

    // ===== Sorcery after resolution =====

    @Test
    @DisplayName("Praetor's Grasp goes to caster's graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        gd.playerDecks.get(player2.getId()).clear();

        harness.setHand(player1, List.of(new PraetorsGrasp()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Praetor's Grasp"));
    }

    // ===== Play from exile =====

    @Test
    @DisplayName("Caster can play exiled card from exile")
    void canPlayExiledCard() {
        Card bears = new GrizzlyBears();
        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).add(bears);

        harness.setHand(player1, List.of(new PraetorsGrasp()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        gs.handleLibraryCardChosen(gd, player1, 0);

        // Now player1 should be able to cast the exiled Grizzly Bears
        harness.addMana(player1, ManaColor.GREEN, 2);

        gs.playCardFromExile(gd, player1, bears.getId(), null, null);
        harness.passBothPriorities();

        // Bears should be on player1's battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));

        // Play permission should be removed
        assertThat(gd.exilePlayPermissions).doesNotContainKey(bears.getId());

        // Card should no longer be in exile
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getId().equals(bears.getId()));
    }

    @Test
    @DisplayName("Exiled card is removed from exile when played")
    void exiledCardRemovedFromExile() {
        Card swamp = new Swamp();
        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).add(swamp);

        harness.setHand(player1, List.of(new PraetorsGrasp()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        gs.handleLibraryCardChosen(gd, player1, 0);

        // Play the exiled land
        gs.playCardFromExile(gd, player1, swamp.getId(), null, null);

        // Land should be on player1's battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Swamp"));

        // Should count as a land play
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
    }
}
