package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.PoisonAtNextUpkeepUnlessPays;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SabertoothCobraTest extends BaseCardTest {

    private int poison() {
        return gd.playerPoisonCounters.getOrDefault(player2.getId(), 0);
    }

    /** Player1's Sabertooth Cobra deals its combat damage to player2. */
    private void dealCombatDamageToPlayer2() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities(); // resolve the two damage triggers
        harness.passBothPriorities();
    }

    /** Advance to player2's upkeep and resolve the delayed obligation into the pay-or-poison prompt. */
    private void advanceToPlayer2UpkeepObligation() {
        gd.turnNumber = 2;
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // UNTAP -> UPKEEP: delayed trigger onto the stack
        harness.passBothPriorities(); // resolve it -> pay-or-poison prompt
    }

    @Test
    @DisplayName("Combat damage gives the damaged player a poison counter immediately")
    void damageGivesPoison() {
        Permanent cobra = addCreatureReady(player1, new SabertoothCobra());
        cobra.setAttacking(true);

        dealCombatDamageToPlayer2();

        assertThat(poison()).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining to pay {2} gives another poison counter at the damaged player's next upkeep")
    void declineGivesSecondPoison() {
        Permanent cobra = addCreatureReady(player1, new SabertoothCobra());
        cobra.setAttacking(true);

        dealCombatDamageToPlayer2();
        advanceToPlayer2UpkeepObligation();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(poison()).isEqualTo(2);
    }

    @Test
    @DisplayName("Paying {2} before the upkeep avoids the second poison counter")
    void payAvoidsSecondPoison() {
        Permanent cobra = addCreatureReady(player1, new SabertoothCobra());
        cobra.setAttacking(true);

        dealCombatDamageToPlayer2();
        advanceToPlayer2UpkeepObligation();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.addMana(player2, ManaColor.WHITE, 2); // mana empties between steps — add it at payment time
        harness.handleMayAbilityChosen(player2, true);

        assertThat(poison()).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.WHITE)).isZero();
    }

    @Test
    @DisplayName("No poison and no upkeep obligation when the Cobra is blocked")
    void blockedCreatesNothing() {
        Permanent cobra = addCreatureReady(player1, new SabertoothCobra());
        cobra.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        dealCombatDamageToPlayer2();

        assertThat(poison()).isZero();
        assertThat(gd.getDelayedActions(PoisonAtNextUpkeepUnlessPays.class)).isEmpty();
    }
}
