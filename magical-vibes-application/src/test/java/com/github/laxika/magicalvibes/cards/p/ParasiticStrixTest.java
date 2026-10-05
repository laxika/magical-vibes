package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.s.SalvageSlasher;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ParasiticStrix.class, SalvageSlasher.class})
class ParasiticStrixTest extends BaseCardTest {

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

    @Test
    @DisplayName("ETB does NOT trigger without a black permanent — no target prompt, no life change")
    void etbDoesNotTriggerWithoutBlackPermanent() {
        castParasiticStrix();
        harness.passBothPriorities(); // resolve creature spell

        // Intervening-if failed (CR 603.4): no trigger, no target prompt.
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();

        harness.assertOnBattlefield(player1, "Parasitic Strix");

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("ETB does nothing if the black permanent is gone before resolution")
    void etbFizzlesWhenGateLost() {
        setupBlackPermanent();
        castParasiticStrix();
        harness.passBothPriorities(); // resolve creature spell — trigger-time target prompt
        harness.handlePermanentChosen(player1, player2.getId()); // ETB trigger on stack

        // Remove the black permanent before the ETB resolves.
        gd.playerBattlefields.get(player1.getId()).removeIf(
                p -> p.getCard().getName().equals("Salvage Slasher"));

        harness.passBothPriorities(); // resolve ETB trigger — gate no longer met

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("An opponent's black permanent does not satisfy the condition")
    void opponentsBlackPermanentDoesNotEnableTrigger() {
        harness.addToBattlefield(player2, new SalvageSlasher());
        castParasiticStrix();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Black cards in hand and graveyard do not satisfy the condition")
    void blackCardsOutsideBattlefieldDoNotEnableTrigger() {
        castParasiticStrix();
        harness.setHand(player1, List.of(new SalvageSlasher()));
        harness.setGraveyard(player1, List.of(new SalvageSlasher()));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Gaining a black permanent after entry does not create a missed trigger")
    void blackPermanentAcquiredAfterEntryDoesNotTrigger() {
        castParasiticStrix();
        harness.passBothPriorities();
        setupBlackPermanent();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A different black permanent can satisfy the condition at resolution")
    void replacementBlackPermanentEnablesResolution() {
        setupBlackPermanent();
        castParasiticStrix();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());

        gd.playerBattlefields.get(player1.getId()).removeIf(
                p -> p.getCard() instanceof SalvageSlasher);
        setupBlackPermanent();
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("The drain resolves after Parasitic Strix leaves the battlefield")
    void triggerResolvesWithoutSource() {
        setupBlackPermanent();
        castParasiticStrix();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());

        gd.playerBattlefields.get(player1.getId()).removeIf(
                p -> p.getCard() instanceof ParasiticStrix);
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    private void setupBlackPermanent() {
        harness.addToBattlefield(player1, new SalvageSlasher());
    }

    private void castParasiticStrix() {
        harness.castFromHand(player1, new ParasiticStrix(), "{2}{U}");
    }
}
