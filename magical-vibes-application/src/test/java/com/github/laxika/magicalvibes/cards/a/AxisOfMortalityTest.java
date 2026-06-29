package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.p.PlatinumEmperion;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ExchangeTargetPlayersLifeTotalsEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AxisOfMortalityTest extends BaseCardTest {

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances to UPKEEP
    }

    /**
     * Triggers Axis of Mortality's upkeep ability, selects two players as targets,
     * and optionally accepts/declines the may effect, then resolves.
     */
    private void triggerAndTargetBothPlayers(Player controller, Player firstTarget, Player secondTarget, boolean accept) {
        advanceToUpkeep(controller);
        // Choose first target player
        harness.handlePermanentChosen(controller, firstTarget.getId());
        // Choose second target player
        harness.handlePermanentChosen(controller, secondTarget.getId());
        // Trigger is now on the stack — pass priorities to resolve
        harness.passBothPriorities();
        // MayEffect prompts the controller — accept or decline
        harness.handleMayAbilityChosen(controller, accept);
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Axis of Mortality has correct upkeep trigger with MayEffect wrapping ExchangeTargetPlayersLifeTotalsEffect")
    void hasCorrectEffects() {
        AxisOfMortality card = new AxisOfMortality();

        assertThat(card.getEffects(EffectSlot.UPKEEP_TRIGGERED)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.UPKEEP_TRIGGERED).getFirst())
                .isInstanceOf(MayEffect.class);
        MayEffect may = (MayEffect) card.getEffects(EffectSlot.UPKEEP_TRIGGERED).getFirst();
        assertThat(may.wrapped()).isInstanceOf(ExchangeTargetPlayersLifeTotalsEffect.class);
    }

    // ===== Exchange life totals (accepted) =====

    @Test
    @DisplayName("Exchanges life totals when controller is lower and accepts")
    void exchangesWhenControllerLower() {
        harness.addToBattlefield(player1, new AxisOfMortality());
        harness.setLife(player1, 5);
        harness.setLife(player2, 20);

        triggerAndTargetBothPlayers(player1, player1, player2, true);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 5);
    }

    @Test
    @DisplayName("Exchanges life totals when controller is higher and accepts")
    void exchangesWhenControllerHigher() {
        harness.addToBattlefield(player1, new AxisOfMortality());
        harness.setLife(player1, 30);
        harness.setLife(player2, 10);

        triggerAndTargetBothPlayers(player1, player1, player2, true);

        harness.assertLife(player1, 10);
        harness.assertLife(player2, 30);
    }

    @Test
    @DisplayName("Exchange with equal life totals results in no change")
    void exchangeWithEqualLifeTotals() {
        harness.addToBattlefield(player1, new AxisOfMortality());
        harness.setLife(player1, 15);
        harness.setLife(player2, 15);

        triggerAndTargetBothPlayers(player1, player1, player2, true);

        harness.assertLife(player1, 15);
        harness.assertLife(player2, 15);
    }

    // ===== Declined exchange =====

    @Test
    @DisplayName("No exchange when controller declines the may ability")
    void noExchangeWhenDeclined() {
        harness.addToBattlefield(player1, new AxisOfMortality());
        harness.setLife(player1, 5);
        harness.setLife(player2, 20);

        triggerAndTargetBothPlayers(player1, player1, player2, false);

        harness.assertLife(player1, 5);
        harness.assertLife(player2, 20);
    }

    // ===== Stack behavior =====

    @Test
    @DisplayName("Trigger puts ability on the stack with both player targets")
    void triggerPutsOnStack() {
        harness.addToBattlefield(player1, new AxisOfMortality());

        advanceToUpkeep(player1);
        // Choose both targets
        harness.handlePermanentChosen(player1, player1.getId());
        harness.handlePermanentChosen(player1, player2.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Axis of Mortality");
        assertThat(entry.getTargetIds()).containsExactly(player1.getId(), player2.getId());
    }

    // ===== Does not trigger during opponent's upkeep =====

    @Test
    @DisplayName("Does not trigger during opponent's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new AxisOfMortality());
        harness.setLife(player1, 5);
        harness.setLife(player2, 20);

        advanceToUpkeep(player2);
        // No pending target selection — the ability should not have triggered
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 5);
        harness.assertLife(player2, 20);
    }

    // ===== Life total can't change (Platinum Emperion) =====

    @Test
    @DisplayName("Exchange does not occur when first targeted player's life can't change")
    void exchangeBlockedWhenFirstPlayerLifeCantChange() {
        harness.addToBattlefield(player1, new AxisOfMortality());
        harness.addToBattlefield(player1, new PlatinumEmperion());
        harness.setLife(player1, 5);
        harness.setLife(player2, 20);

        triggerAndTargetBothPlayers(player1, player1, player2, true);

        harness.assertLife(player1, 5);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Exchange does not occur when second targeted player's life can't change")
    void exchangeBlockedWhenSecondPlayerLifeCantChange() {
        harness.addToBattlefield(player1, new AxisOfMortality());
        harness.addToBattlefield(player2, new PlatinumEmperion());
        harness.setLife(player1, 5);
        harness.setLife(player2, 20);

        triggerAndTargetBothPlayers(player1, player1, player2, true);

        harness.assertLife(player1, 5);
        harness.assertLife(player2, 20);
    }
}
