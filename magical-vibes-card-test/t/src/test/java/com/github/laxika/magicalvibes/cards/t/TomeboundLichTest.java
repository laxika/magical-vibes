package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TomeboundLich.class, Forest.class, Island.class, GreenwoodSentinel.class})
class TomeboundLichTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield draws a card, then discards a card")
    void entersDrawsThenDiscards() {
        harness.setLibrary(player1, List.of(new Island()));
        harness.setHand(player1, new ArrayList<>(List.of(new TomeboundLich(), new GreenwoodSentinel())));
        addManaToCast();

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card.getName().equals("Island"));

        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Greenwood Sentinel");
        harness.assertInHand(player1, "Island");
    }

    @Test
    @DisplayName("Combat damage to a player draws a card, then discards a card")
    void combatDamageDrawsThenDiscards() {
        Permanent lich = addCreatureReady(player1, new TomeboundLich());
        lich.setAttacking(true);
        harness.setLibrary(player1, List.of(new Island()));
        harness.setHand(player1, new ArrayList<>(List.of(new Forest())));

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card.getName().equals("Island"));

        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Forest");
        harness.assertInHand(player1, "Island");
    }

    @Test
    @DisplayName("With no other cards in hand, the entry trigger discards the card just drawn")
    void emptyHandDiscardsDrawnCard() {
        harness.setHand(player1, List.of(new TomeboundLich()));
        harness.setLibrary(player1, List.of(new Island()));
        addManaToCast();

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        if (gd.interaction.isAwaitingInput()) {
            harness.handleCardChosen(player1, 0);
        }

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Island");
        harness.assertOnBattlefield(player1, "Tomebound Lich");
    }

    @Test
    @DisplayName("The defending creature dies to deathtouch without triggering a loot")
    void blockedCombatDoesNotLoot() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new Island()));
        Permanent lich = addCreatureReady(player1, new TomeboundLich());
        lich.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GreenwoodSentinel());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Tomebound Lich");
        harness.assertInGraveyard(player2, "Greenwood Sentinel");
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An opponent-controlled Lich loots and gains life for its controller")
    void opponentControlledLichLootsForOpponent() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Island()));
        Permanent lich = addCreatureReady(player2, new TomeboundLich());
        lich.setAttacking(true);

        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        harness.assertInGraveyard(player2, "Forest");
        harness.assertInHand(player2, "Island");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertLife(player2, 21);
        harness.assertLife(player1, 19);
    }

    private void addManaToCast() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
