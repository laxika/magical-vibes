package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BleakCovenVampires.class, Memnite.class})
class BleakCovenVampiresTest extends BaseCardTest {

    @Test
    @DisplayName("ETB target is chosen as the trigger goes on the stack, not at cast time")
    void etbTargetChosenAtTriggerTime() {
        setupMetalcraft();
        castBleakCovenVampires();

        // Casting the creature never asks for a target (CR 601.2c) — the stack holds
        // only the creature spell, with no target attached.
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isNull();

        harness.passBothPriorities(); // resolve creature spell

        // Metalcraft is met, so the trigger fires and the controller is prompted
        // for the target as the ability is put on the stack (CR 603.3d).
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("ETB triggers drain when metalcraft is met (3+ artifacts)")
    void etbTriggersWithMetalcraft() {
        setupMetalcraft();
        castBleakCovenVampires();
        harness.passBothPriorities(); // resolve creature spell — trigger-time target prompt
        harness.handlePermanentChosen(player1, player2.getId());

        // ETB trigger should be on the stack with the chosen target
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Bleak Coven Vampires");
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("ETB drain resolves: target loses 4 life, controller gains 4 life")
    void etbDrainsLifeWithMetalcraft() {
        setupMetalcraft();
        castBleakCovenVampires();
        harness.passBothPriorities(); // resolve creature spell — trigger-time target prompt
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities(); // resolve ETB trigger

        harness.assertLife(player2, 16);
        harness.assertLife(player1, 24);
    }

    @Test
    @DisplayName("ETB drain works with non-default life totals")
    void etbDrainsLifeWithCustomTotals() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 15);

        setupMetalcraft();
        castBleakCovenVampires();
        harness.passBothPriorities(); // resolve creature spell — trigger-time target prompt
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities(); // resolve ETB trigger

        harness.assertLife(player2, 11);
        harness.assertLife(player1, 14);
    }

    @Test
    @DisplayName("Game log records life drain with metalcraft")
    void gameLogRecordsLifeChanges() {
        setupMetalcraft();
        castBleakCovenVampires();
        harness.passBothPriorities(); // resolve creature spell — trigger-time target prompt
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("loses 4 life"));
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("gains 4 life"));
    }

    @Test
    @DisplayName("ETB does NOT trigger without metalcraft (0 artifacts) — no target prompt")
    void etbDoesNotTriggerWithoutMetalcraft() {
        castBleakCovenVampires();
        harness.passBothPriorities(); // resolve creature spell

        // The intervening-if failed (CR 603.4): no trigger, and the controller is
        // never asked for a target.
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();

        // Creature is still on the battlefield
        harness.assertOnBattlefield(player1, "Bleak Coven Vampires");

        // Life totals unchanged
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("ETB does NOT trigger with only 2 artifacts")
    void etbDoesNotTriggerWithTwoArtifacts() {
        harness.addToBattlefield(player1, new Memnite());
        harness.addToBattlefield(player1, new Memnite());

        castBleakCovenVampires();
        harness.passBothPriorities(); // resolve creature spell

        // No ETB trigger and no target prompt
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();

        // Life totals unchanged
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("ETB does nothing if metalcraft is lost before resolution")
    void etbFizzlesWhenMetalcraftLost() {
        setupMetalcraft();
        castBleakCovenVampires();
        harness.passBothPriorities(); // resolve creature spell — trigger-time target prompt
        harness.handlePermanentChosen(player1, player2.getId()); // ETB trigger on stack

        // Remove artifacts before ETB resolves (simulating opponent destroying them)
        gd.playerBattlefields.get(player1.getId()).removeIf(
                p -> p.getCard().getName().equals("Memnite"));

        harness.passBothPriorities(); // resolve ETB trigger — metalcraft no longer met

        // Life totals unchanged (ability does nothing)
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("metalcraft ability does nothing"));
    }

    @Test
    @DisplayName("Creature enters battlefield even without metalcraft")
    void creatureEntersWithoutMetalcraft() {
        castBleakCovenVampires();
        harness.passBothPriorities(); // resolve creature spell

        harness.assertOnBattlefield(player1, "Bleak Coven Vampires");
    }

    @Test
    @DisplayName("Stack is empty after full resolution with metalcraft")
    void stackEmptyAfterResolution() {
        setupMetalcraft();
        castBleakCovenVampires();
        harness.passBothPriorities(); // resolve creature spell — trigger-time target prompt
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The controller can target themselves and survive briefly falling below zero life")
    void canTargetController() {
        setupMetalcraft();
        harness.setLife(player1, 3);
        castBleakCovenVampires();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 3);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("loses 4 life"));
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("gains 4 life"));
    }

    @Test
    @DisplayName("Opponent artifacts do not contribute to metalcraft")
    void opponentArtifactsDoNotCount() {
        harness.addToBattlefield(player1, new Memnite());
        harness.addToBattlefield(player1, new Memnite());
        harness.addToBattlefield(player2, new Memnite());
        castBleakCovenVampires();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The trigger still resolves after losing an artifact when three remain")
    void resolvesWhenThreeArtifactsRemain() {
        setupMetalcraft();
        harness.addToBattlefield(player1, new Memnite());
        castBleakCovenVampires();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        gd.playerBattlefields.get(player1.getId()).removeFirst();
        harness.passBothPriorities();

        harness.assertLife(player1, 24);
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("The trigger resolves even if its source leaves the battlefield")
    void resolvesWithoutSource() {
        setupMetalcraft();
        castBleakCovenVampires();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        gd.playerBattlefields.get(player1.getId()).removeIf(
                permanent -> permanent.getCard() instanceof BleakCovenVampires);
        harness.passBothPriorities();

        harness.assertLife(player1, 24);
        harness.assertLife(player2, 16);
    }

    private void setupMetalcraft() {
        harness.addToBattlefield(player1, new Memnite());
        harness.addToBattlefield(player1, new Memnite());
        harness.addToBattlefield(player1, new Memnite());
    }

    private void castBleakCovenVampires() {
        harness.setHand(player1, List.of(new BleakCovenVampires()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castCreature(player1, 0);
    }
}
