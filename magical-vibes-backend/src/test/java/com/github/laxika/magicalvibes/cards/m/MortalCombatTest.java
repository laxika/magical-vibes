package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.WinGameIfCreaturesInGraveyardEffect;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MortalCombatTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;
    private GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gd = harness.getGameData();
        harness.skipMulligan();
        harness.clearMessages();
    }

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances to UPKEEP
    }

    private List<Card> createCreatureCards(int count) {
        List<Card> creatures = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            creatures.add(new GrizzlyBears());
        }
        return creatures;
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Mortal Combat has correct card properties")
    void hasCorrectProperties() {
        MortalCombat card = new MortalCombat();

        assertThat(card.getName()).isEqualTo("Mortal Combat");
        assertThat(card.getType()).isEqualTo(CardType.ENCHANTMENT);
        assertThat(card.getManaCost()).isEqualTo("{2}{B}{B}");
        assertThat(card.getColor()).isEqualTo(CardColor.BLACK);
        assertThat(card.getEffects(EffectSlot.UPKEEP_TRIGGERED)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.UPKEEP_TRIGGERED).getFirst())
                .isInstanceOf(WinGameIfCreaturesInGraveyardEffect.class);
        WinGameIfCreaturesInGraveyardEffect effect =
                (WinGameIfCreaturesInGraveyardEffect) card.getEffects(EffectSlot.UPKEEP_TRIGGERED).getFirst();
        assertThat(effect.threshold()).isEqualTo(20);
    }

    // ===== Trigger with 20+ creatures =====

    @Test
    @DisplayName("Triggers and wins when exactly 20 creature cards are in graveyard")
    void winsWithExactlyTwentyCreatures() {
        harness.addToBattlefield(player1, new MortalCombat());
        harness.setGraveyard(player1, createCreatureCards(20));

        advanceToUpkeep(player1);

        // Trigger should be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Mortal Combat");

        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.gameLog).anyMatch(l -> l.contains("wins the game"));
    }

    @Test
    @DisplayName("Triggers and wins when more than 20 creature cards are in graveyard")
    void winsWithMoreThanTwentyCreatures() {
        harness.addToBattlefield(player1, new MortalCombat());
        harness.setGraveyard(player1, createCreatureCards(25));

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    // ===== Intervening-if: does NOT trigger with fewer than 20 =====

    @Test
    @DisplayName("Does not trigger when only 19 creature cards are in graveyard")
    void doesNotTriggerWithNineteenCreatures() {
        harness.addToBattlefield(player1, new MortalCombat());
        harness.setGraveyard(player1, createCreatureCards(19));

        advanceToUpkeep(player1);

        // Trigger should NOT be on the stack (intervening-if fails)
        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Does not trigger with empty graveyard")
    void doesNotTriggerWithEmptyGraveyard() {
        harness.addToBattlefield(player1, new MortalCombat());

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    // ===== Only creature cards count =====

    @Test
    @DisplayName("Only creature cards count — non-creature cards in graveyard are ignored")
    void onlyCreatureCardsCount() {
        harness.addToBattlefield(player1, new MortalCombat());

        List<Card> graveyard = new ArrayList<>();
        graveyard.addAll(createCreatureCards(19));
        graveyard.add(new Plains()); // land, not a creature
        harness.setGraveyard(player1, graveyard);

        advanceToUpkeep(player1);

        // 19 creatures + 1 land = 20 cards, but only 19 are creatures
        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("20 creatures plus non-creature cards still triggers")
    void twentyCreaturesPlusNonCreaturesStillTriggers() {
        harness.addToBattlefield(player1, new MortalCombat());

        List<Card> graveyard = new ArrayList<>();
        graveyard.addAll(createCreatureCards(20));
        graveyard.add(new Plains());
        graveyard.add(new MindRot());
        harness.setGraveyard(player1, graveyard);

        advanceToUpkeep(player1);

        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    // ===== Intervening-if at resolution =====

    @Test
    @DisplayName("Condition checked again on resolution — does nothing if no longer met")
    void interveningIfCheckedOnResolution() {
        harness.addToBattlefield(player1, new MortalCombat());
        harness.setGraveyard(player1, createCreatureCards(20));

        advanceToUpkeep(player1);

        // Trigger is on the stack
        assertThat(gd.stack).hasSize(1);

        // Opponent removes creatures from graveyard before resolution (simulating interaction)
        gd.playerGraveyards.get(player1.getId()).subList(0, 5).clear(); // remove 5, leaving 15

        harness.passBothPriorities(); // resolve trigger

        // Condition no longer met — game should NOT be finished
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
        assertThat(gd.gameLog).anyMatch(l -> l.contains("condition is no longer met"));
    }

    // ===== Does not trigger on opponent's upkeep =====

    @Test
    @DisplayName("Does not trigger during opponent's upkeep")
    void doesNotTriggerOnOpponentsUpkeep() {
        harness.addToBattlefield(player1, new MortalCombat());
        harness.setGraveyard(player1, createCreatureCards(20));

        // Advance to player2's upkeep — Mortal Combat is on player1's battlefield
        advanceToUpkeep(player2);

        // Should not trigger because it's not the controller's upkeep
        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    // ===== Opponent's graveyard does not count =====

    @Test
    @DisplayName("Opponent's graveyard creatures do not count toward threshold")
    void opponentsGraveyardDoesNotCount() {
        harness.addToBattlefield(player1, new MortalCombat());
        harness.setGraveyard(player1, createCreatureCards(10));
        harness.setGraveyard(player2, createCreatureCards(20)); // opponent has 20 creatures

        advanceToUpkeep(player1);

        // Only controller's graveyard matters
        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    // ===== Game log =====

    @Test
    @DisplayName("Game log mentions creature count and card name on win")
    void gameLogMentionsDetails() {
        harness.addToBattlefield(player1, new MortalCombat());
        harness.setGraveyard(player1, createCreatureCards(20));

        advanceToUpkeep(player1);

        // Log mentions the trigger
        assertThat(gd.gameLog).anyMatch(l -> l.contains("Mortal Combat") && l.contains("triggers"));

        harness.passBothPriorities();

        // Log mentions creature count and winning
        assertThat(gd.gameLog).anyMatch(l -> l.contains("20 creature cards") && l.contains("wins the game"));
    }
}

