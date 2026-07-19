package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ParasiticStrixTest extends BaseCardTest {

    // ===== ETB with a black permanent controlled =====

    @Test
    @DisplayName("ETB target is chosen as the trigger goes on the stack, not at cast time")
    void etbTargetChosenAtTriggerTime() {
        setupBlackPermanent();
        castParasiticStrix();

        // Casting the creature never asks for a target (CR 601.2c).
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isNull();

        harness.passBothPriorities(); // resolve creature spell

        // Gate is met, so the trigger fires and the controller is prompted for the
        // target as the ability is put on the stack (CR 603.3d).
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("ETB trigger goes on the stack with the chosen target when a black permanent is controlled")
    void etbTriggersWithBlackPermanent() {
        setupBlackPermanent();
        castParasiticStrix();
        harness.passBothPriorities(); // resolve creature spell — trigger-time target prompt
        harness.handlePermanentChosen(player1, player2.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("ETB drain resolves: target loses 2 life, controller gains 2 life")
    void etbDrainsLife() {
        setupBlackPermanent();
        castParasiticStrix();
        harness.passBothPriorities(); // resolve creature spell — trigger-time target prompt
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Controller may target themselves for the drain")
    void etbCanTargetSelf() {
        setupBlackPermanent();
        castParasiticStrix();
        harness.passBothPriorities(); // resolve creature spell — trigger-time target prompt
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities(); // resolve ETB trigger

        // Loses 2 then gains 2 — net zero for the controller.
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Game log records the life drain")
    void gameLogRecordsLifeChanges() {
        setupBlackPermanent();
        castParasiticStrix();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("loses 2 life"));
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("gains 2 life"));
    }

    // ===== ETB without a black permanent =====

    @Test
    @DisplayName("ETB does NOT trigger without a black permanent — no target prompt, no life change")
    void etbDoesNotTriggerWithoutBlackPermanent() {
        castParasiticStrix();
        harness.passBothPriorities(); // resolve creature spell

        // Intervening-if failed (CR 603.4): no trigger, no target prompt.
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Parasitic Strix"));

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    // ===== Gate lost before resolution =====

    @Test
    @DisplayName("ETB does nothing if the black permanent is gone before resolution")
    void etbFizzlesWhenGateLost() {
        setupBlackPermanent();
        castParasiticStrix();
        harness.passBothPriorities(); // resolve creature spell — trigger-time target prompt
        harness.handlePermanentChosen(player1, player2.getId()); // ETB trigger on stack

        // Remove the black permanent before the ETB resolves.
        gd.playerBattlefields.get(player1.getId()).removeIf(
                p -> p.getCard().getName().equals("Scathe Zombies"));

        harness.passBothPriorities(); // resolve ETB trigger — gate no longer met

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    // ===== Helpers =====

    private void setupBlackPermanent() {
        harness.addToBattlefield(player1, new ScatheZombies());
    }

    private void castParasiticStrix() {
        harness.setHand(player1, List.of(new ParasiticStrix()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.getGameService().playCard(gd, player1, 0, 0, null, null);
    }
}
