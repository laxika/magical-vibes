package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.Distress;
import com.github.laxika.magicalvibes.cards.m.MindRot;
import com.github.laxika.magicalvibes.cards.s.Sift;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GuerrillaTacticsTest {

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

    // ===== Card properties =====

    @Test
    @DisplayName("Guerrilla Tactics has correct card properties")
    void hasCorrectProperties() {
        GuerrillaTactics card = new GuerrillaTactics();

        assertThat(card.getName()).isEqualTo("Guerrilla Tactics");
        assertThat(card.getType()).isEqualTo(CardType.INSTANT);
        assertThat(card.getManaCost()).isEqualTo("{1}{R}");
        assertThat(card.getColor()).isEqualTo(CardColor.RED);
        assertThat(card.isNeedsTarget()).isTrue();
    }

    @Test
    @DisplayName("Guerrilla Tactics has spell effect dealing 2 damage")
    void hasSpellEffect() {
        GuerrillaTactics card = new GuerrillaTactics();

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst())
                .isInstanceOf(DealDamageToAnyTargetEffect.class);
        DealDamageToAnyTargetEffect effect =
                (DealDamageToAnyTargetEffect) card.getEffects(EffectSlot.SPELL).getFirst();
        assertThat(effect.damage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Guerrilla Tactics has discard trigger dealing 4 damage")
    void hasDiscardTrigger() {
        GuerrillaTactics card = new GuerrillaTactics();

        assertThat(card.getEffects(EffectSlot.ON_SELF_DISCARDED_BY_OPPONENT)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_SELF_DISCARDED_BY_OPPONENT).getFirst())
                .isInstanceOf(DealDamageToAnyTargetEffect.class);
        DealDamageToAnyTargetEffect discardEffect =
                (DealDamageToAnyTargetEffect) card.getEffects(EffectSlot.ON_SELF_DISCARDED_BY_OPPONENT).getFirst();
        assertThat(discardEffect.damage()).isEqualTo(4);
    }

    // ===== Casting as a spell =====

    @Test
    @DisplayName("Casting targeting a player puts it on the stack")
    void castingTargetingPlayerPutsItOnStack() {
        harness.setHand(player1, List.of(new GuerrillaTactics()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Guerrilla Tactics");
        assertThat(entry.getTargetPermanentId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Casting targeting a creature puts it on the stack")
    void castingTargetingCreaturePutsItOnStack() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GuerrillaTactics()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getTargetPermanentId()).isEqualTo(targetId);
    }

    // ===== Spell damage (2 damage) =====

    @Test
    @DisplayName("Deals 2 damage to target player")
    void deals2DamageToPlayer() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new GuerrillaTactics()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Deals 2 damage to target creature, destroying a 2/2")
    void deals2DamageToCreatureDestroysIt() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GuerrillaTactics()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Goes to graveyard after resolving as spell")
    void goesToGraveyardAfterResolving() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new GuerrillaTactics()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Guerrilla Tactics"));
    }

    // ===== Discard trigger — opponent forces discard via Distress (revealed hand choice) =====

    @Test
    @DisplayName("Triggers when opponent discards it via Distress, prompting for any target")
    void triggersWhenDiscardedByOpponentViaDistress() {
        harness.setHand(player2, new ArrayList<>(List.of(new GuerrillaTactics())));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new Distress()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Player1 chooses Guerrilla Tactics from player2's revealed hand
        harness.handleCardChosen(player1, 0);

        // Guerrilla Tactics' discard trigger should prompt player2 to choose any target
        assertThat(gd.awaitingInput).isEqualTo(AwaitingInput.PERMANENT_CHOICE);
        assertThat(gd.awaitingPermanentChoicePlayerId).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Discard trigger deals 4 damage to chosen player target")
    void discardTriggerDeals4DamageToPlayer() {
        harness.setHand(player2, new ArrayList<>(List.of(new GuerrillaTactics())));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new Distress()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Player1 chooses Guerrilla Tactics from player2's revealed hand
        harness.handleCardChosen(player1, 0);

        // Player2 chooses player1 as the target for the 4 damage trigger
        harness.handlePermanentChosen(player2, player1.getId());

        // Triggered ability goes on the stack, pass priority to resolve it
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Discard trigger deals 4 damage to chosen creature target")
    void discardTriggerDeals4DamageToCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player2, new ArrayList<>(List.of(new GuerrillaTactics())));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new Distress()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Player1 chooses Guerrilla Tactics from player2's revealed hand
        harness.handleCardChosen(player1, 0);

        // Player2 targets the creature with the 4 damage trigger
        harness.handlePermanentChosen(player2, bearsId);

        // Triggered ability goes on the stack, pass priority to resolve it
        harness.passBothPriorities();

        // Grizzly Bears (2/2) should be destroyed by 4 damage
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Discard trigger can target self (controller of the trigger)")
    void discardTriggerCanTargetSelf() {
        harness.setHand(player2, new ArrayList<>(List.of(new GuerrillaTactics())));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new Distress()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);

        // Player2 targets themselves with the 4 damage
        harness.handlePermanentChosen(player2, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    // ===== Discard trigger — opponent forces discard via Mind Rot (handleDiscardCardChosen path) =====

    @Test
    @DisplayName("Triggers when discarded by Mind Rot")
    void triggersWhenDiscardedByMindRot() {
        harness.setHand(player2, new ArrayList<>(List.of(new GuerrillaTactics(), new GrizzlyBears())));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new MindRot()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Player2 discards Guerrilla Tactics first
        harness.handleCardChosen(player2, 0);

        // Player2 discards Grizzly Bears second
        harness.handleCardChosen(player2, 0);

        // Guerrilla Tactics' discard trigger should prompt player2 to choose any target
        assertThat(gd.awaitingInput).isEqualTo(AwaitingInput.PERMANENT_CHOICE);
        assertThat(gd.awaitingPermanentChoicePlayerId).isEqualTo(player2.getId());

        // Player2 chooses player1 as target
        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16);
    }

    // ===== No trigger on self-discard =====

    @Test
    @DisplayName("Does NOT trigger when controller discards it themselves via Sift")
    void doesNotTriggerOnSelfDiscard() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        // Player1 casts Sift (draw 3, discard 1) — self-discard
        gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());
        gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());
        gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());

        harness.setHand(player1, List.of(new Sift(), new GuerrillaTactics()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities(); // Resolve Sift — draws 3, prompts for discard

        // Player1 discards Guerrilla Tactics (now at some index in hand)
        // After Sift draws 3 cards, hand is: [GuerrillaTactics, drawn1, drawn2, drawn3]
        harness.handleCardChosen(player1, 0); // Discard the Guerrilla Tactics

        // No trigger — self-discard does not activate the ability
        assertThat(gd.awaitingInput).isNull();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    // ===== Guerrilla Tactics in graveyard after discard trigger =====

    @Test
    @DisplayName("Guerrilla Tactics is in graveyard after being discarded and trigger resolves")
    void inGraveyardAfterDiscardTriggerResolves() {
        harness.setHand(player2, new ArrayList<>(List.of(new GuerrillaTactics())));
        harness.setLife(player1, 20);

        harness.setHand(player1, List.of(new Distress()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);
        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();

        // Guerrilla Tactics should be in player2's graveyard
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Guerrilla Tactics"));
        // Player2's hand should be empty
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    // ===== Stack correctness for triggered ability =====

    @Test
    @DisplayName("Triggered ability is put on the stack as TRIGGERED_ABILITY")
    void triggeredAbilityOnStack() {
        harness.setHand(player2, new ArrayList<>(List.of(new GuerrillaTactics())));
        harness.setLife(player1, 20);

        harness.setHand(player1, List.of(new Distress()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);

        // Player2 targets player1
        harness.handlePermanentChosen(player2, player1.getId());

        // Triggered ability should be on the stack
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Guerrilla Tactics");
        assertThat(entry.getTargetPermanentId()).isEqualTo(player1.getId());
    }

    // ===== Interaction with Megrim =====

    @Test
    @DisplayName("Both Megrim and Guerrilla Tactics trigger when opponent forces discard")
    void interactsWithMegrim() {
        com.github.laxika.magicalvibes.cards.m.Megrim megrim = new com.github.laxika.magicalvibes.cards.m.Megrim();
        harness.addToBattlefield(player1, megrim);
        harness.setHand(player2, new ArrayList<>(List.of(new GuerrillaTactics())));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new Distress()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Player1 chooses Guerrilla Tactics from player2's revealed hand
        harness.handleCardChosen(player1, 0);

        // Megrim deals 2 damage to player2 (the discarding player)
        // Guerrilla Tactics trigger prompts player2 to choose any target
        assertThat(gd.awaitingInput).isEqualTo(AwaitingInput.PERMANENT_CHOICE);

        // Player2 targets player1 with the 4 damage
        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();

        // Player1 took 4 damage from Guerrilla Tactics trigger
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16);
        // Player2 took 2 damage from Megrim
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }
}
