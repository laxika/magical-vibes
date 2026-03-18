package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardsTypeToHandRestToGraveyardEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class MulchTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has RevealTopCardsTypeToHandRestToGraveyardEffect on SPELL slot with count=4 and LAND type")
    void hasCorrectStructure() {
        Mulch card = new Mulch();

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst())
                .isInstanceOf(RevealTopCardsTypeToHandRestToGraveyardEffect.class);

        RevealTopCardsTypeToHandRestToGraveyardEffect effect =
                (RevealTopCardsTypeToHandRestToGraveyardEffect) card.getEffects(EffectSlot.SPELL).getFirst();
        assertThat(effect.count()).isEqualTo(4);
        assertThat(effect.cardTypes()).isEqualTo(Set.of(CardType.LAND));
    }

    // ===== All lands go to hand =====

    @Test
    @DisplayName("All revealed land cards go to hand")
    void allLandsGoToHand() {
        Card forest1 = new Forest();
        Card forest2 = new Forest();
        Card island = new Island();
        Card bears = new GrizzlyBears();

        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(forest1, forest2, island, bears));

        harness.setHand(player1, List.of(new Mulch()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // Three lands should be in hand
        assertThat(gd.playerHands.get(player1.getId()))
                .contains(forest1, forest2, island);
        // Creature should be in graveyard
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    // ===== Non-lands go to graveyard =====

    @Test
    @DisplayName("All non-land cards go to graveyard")
    void nonLandsGoToGraveyard() {
        Card shock1 = new Shock();
        Card shock2 = new Shock();
        Card bears = new GrizzlyBears();
        Card forest = new Forest();

        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(shock1, shock2, bears, forest));

        harness.setHand(player1, List.of(new Mulch()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // Forest goes to hand
        assertThat(gd.playerHands.get(player1.getId())).contains(forest);
        // Non-lands go to graveyard (plus Mulch itself)
        harness.assertInGraveyard(player1, "Shock");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId()).stream()
                .filter(c -> c.getName().equals("Shock")).count()).isEqualTo(2);
    }

    // ===== No lands revealed =====

    @Test
    @DisplayName("When no lands are revealed, all cards go to graveyard")
    void noLandsAllToGraveyard() {
        Card shock = new Shock();
        Card bears1 = new GrizzlyBears();
        Card bears2 = new GrizzlyBears();
        Card bears3 = new GrizzlyBears();

        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(shock, bears1, bears2, bears3));

        harness.setHand(player1, List.of(new Mulch()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // No lands in hand (hand should be empty since Mulch was cast)
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        // All four non-lands + Mulch in graveyard
        harness.assertInGraveyard(player1, "Shock");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId()).stream()
                .filter(c -> c.getName().equals("Grizzly Bears")).count()).isEqualTo(3);
    }

    // ===== All lands revealed =====

    @Test
    @DisplayName("When all revealed cards are lands, all go to hand")
    void allLandsRevealed() {
        Card forest1 = new Forest();
        Card forest2 = new Forest();
        Card island1 = new Island();
        Card island2 = new Island();

        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(forest1, forest2, island1, island2));

        harness.setHand(player1, List.of(new Mulch()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // All four lands should be in hand
        assertThat(gd.playerHands.get(player1.getId()))
                .contains(forest1, forest2, island1, island2);
        // Only Mulch itself in graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()).stream()
                .filter(c -> c.getName().equals("Mulch")).count()).isEqualTo(1);
    }

    // ===== Library smaller than 4 =====

    @Test
    @DisplayName("When library has fewer than 4 cards, reveals all available")
    void librarySmallerThanFour() {
        Card forest = new Forest();
        Card bears = new GrizzlyBears();

        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(forest, bears));

        harness.setHand(player1, List.of(new Mulch()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // Forest goes to hand, Bears to graveyard
        assertThat(gd.playerHands.get(player1.getId())).contains(forest);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    // ===== Empty library =====

    @Test
    @DisplayName("Does nothing when library is empty")
    void emptyLibrary() {
        gd.playerDecks.get(player1.getId()).clear();

        harness.setHand(player1, List.of(new Mulch()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // Hand should be empty
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    // ===== No player interaction required =====

    @Test
    @DisplayName("Effect is deterministic — no player interaction required")
    void noInteractionRequired() {
        Card forest = new Forest();
        Card bears = new GrizzlyBears();
        Card shock = new Shock();
        Card island = new Island();

        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(forest, bears, shock, island));

        harness.setHand(player1, List.of(new Mulch()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // Should not be awaiting any input
        assertThat(gd.interaction.awaitingInputType()).isNull();
    }
}
