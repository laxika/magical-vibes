package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.EndTurnEffect;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TimeStopTest {

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
    @DisplayName("Time Stop has correct card properties")
    void hasCorrectProperties() {
        TimeStop card = new TimeStop();

        assertThat(card.getName()).isEqualTo("Time Stop");
        assertThat(card.getType()).isEqualTo(CardType.INSTANT);
        assertThat(card.getManaCost()).isEqualTo("{4}{U}{U}");
        assertThat(card.getColor()).isEqualTo(CardColor.BLUE);
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0)).isInstanceOf(EndTurnEffect.class);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting puts Time Stop on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new TimeStop()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castInstant(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Time Stop");
    }

    // ===== Basic resolution =====

    @Test
    @DisplayName("Resolving with empty stack exiles Time Stop and ends the turn")
    void resolvingEndsTheTurn() {
        harness.setHand(player1, List.of(new TimeStop()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        GameData gd = harness.getGameData();
        UUID activePlayerBefore = gd.activePlayerId;
        int turnBefore = gd.turnNumber;

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        // Time Stop is exiled, not in graveyard
        assertThat(gd.playerExiledCards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Time Stop"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Time Stop"));

        // Turn advanced to next player
        assertThat(gd.activePlayerId).isNotEqualTo(activePlayerBefore);
        assertThat(gd.turnNumber).isEqualTo(turnBefore + 1);
    }

    @Test
    @DisplayName("Stack is empty after resolution")
    void stackIsEmptyAfterResolution() {
        harness.setHand(player1, List.of(new TimeStop()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
    }

    // ===== Exiling spells from the stack =====

    @Test
    @DisplayName("Resolving exiles other spells on the stack")
    void exilesOtherSpellsOnStack() {
        // Player2 casts a creature, then player1 responds with Time Stop
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player2, List.of(bears));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.setHand(player1, List.of(new TimeStop()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player2, 0);
        harness.passPriority(player2);
        harness.castInstant(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();

        // Grizzly Bears is exiled (not on battlefield, not in graveyard)
        assertThat(gd.playerExiledCards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));

        // Time Stop itself is exiled
        assertThat(gd.playerExiledCards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Time Stop"));
    }

    @Test
    @DisplayName("Resolving exiles multiple spells on the stack")
    void exilesMultipleSpellsOnStack() {
        // Player2 casts a creature, lets it resolve, casts another creature,
        // then player1 responds with Time Stop
        GrizzlyBears bears1 = new GrizzlyBears();
        SerraAngel angel = new SerraAngel();
        harness.setHand(player2, List.of(bears1, angel));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.addMana(player2, ManaColor.WHITE, 5);

        harness.setHand(player1, List.of(new TimeStop()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Cast bears, then angel
        harness.castCreature(player2, 0);
        harness.passPriority(player2);
        harness.passPriority(player1);
        // Bears resolves, now cast angel
        harness.castCreature(player2, 0);
        harness.passPriority(player2);

        // Player1 responds with Time Stop
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // Serra Angel is exiled from the stack
        assertThat(gd.playerExiledCards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Serra Angel"));

        // Serra Angel is NOT on the battlefield
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Serra Angel"));

        // Grizzly Bears resolved earlier and is on the battlefield
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    // ===== Combat state =====

    @Test
    @DisplayName("Resolving during combat clears combat state")
    void clearsCombatStateDuringCombat() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, bears);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        GameData gd = harness.getGameData();
        // Simulate a creature that is attacking
        Permanent attackingBears = gd.playerBattlefields.get(player1.getId()).get(0);
        attackingBears.setAttacking(true);

        harness.setHand(player2, List.of(new TimeStop()));
        harness.addMana(player2, ManaColor.BLUE, 6);

        harness.passPriority(player1);
        harness.castInstant(player2, 0);
        harness.passBothPriorities();

        // Combat state is cleared — creature should no longer be attacking
        assertThat(attackingBears.isAttacking()).isFalse();
    }

    // ===== End-of-turn modifiers =====

    @Test
    @DisplayName("Resolving resets end-of-turn modifiers on permanents")
    void resetsEndOfTurnModifiers() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, bears);

        GameData gd = harness.getGameData();
        Permanent perm = gd.playerBattlefields.get(player1.getId()).get(0);
        perm.setPowerModifier(3);
        perm.setToughnessModifier(3);

        harness.setHand(player1, List.of(new TimeStop()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(perm.getPowerModifier()).isZero();
        assertThat(perm.getToughnessModifier()).isZero();
    }

    // ===== Pending may abilities =====

    @Test
    @DisplayName("Resolving clears pending may abilities")
    void clearsPendingMayAbilities() {
        harness.setHand(player1, List.of(new TimeStop()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castInstant(player1, 0);

        // Simulate a pending may ability that exists when Time Stop resolves
        GameData gd = harness.getGameData();
        gd.pendingMayAbilities.add(new com.github.laxika.magicalvibes.model.PendingMayAbility(
                new GrizzlyBears(), player1.getId(), List.of(), "Do something?"
        ));

        harness.passBothPriorities();

        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    // ===== Game log =====

    @Test
    @DisplayName("Game log records that the turn ends")
    void gameLogRecordsTurnEnds() {
        harness.setHand(player1, List.of(new TimeStop()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("The turn ends"));
    }

    @Test
    @DisplayName("Game log records exiled spells")
    void gameLogRecordsExiledSpells() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player2, List.of(bears));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.setHand(player1, List.of(new TimeStop()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player2, 0);
        harness.passPriority(player2);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog).anyMatch(log ->
                log.contains("Grizzly Bears") && log.contains("exiled"));
    }

    // ===== End-of-combat sacrifices =====

    @Test
    @DisplayName("Resolving clears end-of-combat sacrifice list")
    void clearsEndOfCombatSacrifices() {
        harness.setHand(player1, List.of(new TimeStop()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        GameData gd = harness.getGameData();
        gd.permanentsToSacrificeAtEndOfCombat.add(UUID.randomUUID());

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.permanentsToSacrificeAtEndOfCombat).isEmpty();
    }
}

