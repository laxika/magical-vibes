package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HermeticStudy;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.LoseLifeAtNextDrawStepUnlessPays;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NafsAsp.class, GrizzlyBears.class, HermeticStudy.class})
class NafsAspTest extends BaseCardTest {

    private Permanent addReadyAsp() {
        return addCreatureReady(player1, new NafsAsp());
    }

    /** Player1's Nafs Asp deals its 1 combat damage to player2 (queuing the draw-step obligation). */
    private void dealCombatDamageToPlayer2() {
        resolveCombat();
    }

    /** Advance to player2's draw step and resolve any unpaid life-loss obligations. */
    private void advanceToPlayer2DrawStepObligation() {
        gd.turnNumber = 2; // avoid the starting-player turn-1 draw-step skip
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // UPKEEP -> DRAW: turn-based draw + delayed trigger onto the stack
        resolveAllTriggers();
    }

    @Test
    @DisplayName("An unpaid obligation loses 1 life at the damaged player's next draw step")
    void declineLosesLife() {
        Permanent asp = addReadyAsp();
        asp.setAttacking(true);

        dealCombatDamageToPlayer2();
        int lifeAfterCombat = gd.playerLifeTotals.get(player2.getId());

        advanceToPlayer2DrawStepObligation();

        assertThat(gd.interaction.activeInteraction()).isNull();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeAfterCombat - 1);
    }

    @Test
    @DisplayName("Paying during upkeep avoids the draw-step life loss")
    void payAvoidsLife() {
        Permanent asp = addReadyAsp();
        asp.setAttacking(true);

        dealCombatDamageToPlayer2();
        int lifeAfterCombat = gd.playerLifeTotals.get(player2.getId());

        resolveAllTriggers();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.WHITE, 1);
        gs.payDrawStepLifeLoss(gd, player2, asp.getCard().getId());

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.getDelayedActions(LoseLifeAtNextDrawStepUnlessPays.class)).isEmpty();
        assertThat(gd.stack).isEmpty();
        advanceToPlayer2DrawStepObligation();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeAfterCombat);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.WHITE)).isZero();
    }

    @Test
    @DisplayName("Noncombat damage also creates a delayed obligation")
    void noncombatDamageCreatesObligation() {
        Permanent asp = addReadyAsp();
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HermeticStudy());
        aura.setAttachedTo(asp.getId());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.getDelayedActions(LoseLifeAtNextDrawStepUnlessPays.class)).hasSize(1);
    }

    @Test
    @DisplayName("Each separate damage event creates a separate obligation")
    void separateDamageEventsCreateSeparateObligations() {
        Permanent firstAsp = addReadyAsp();
        firstAsp.setAttacking(true);
        Permanent secondAsp = addReadyAsp();
        secondAsp.setAttacking(true);

        dealCombatDamageToPlayer2();
        resolveAllTriggers();
        int lifeAfterCombat = gd.playerLifeTotals.get(player2.getId());

        advanceToPlayer2DrawStepObligation();

        assertThat(gd.interaction.activeInteraction()).isNull();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeAfterCombat - 2);
    }

    @Test
    @DisplayName("The damage trigger does not offer payment after the draw step begins")
    void paymentCannotBeMadeAfterDrawStepBegins() {
        Permanent asp = addReadyAsp();
        asp.setAttacking(true);

        dealCombatDamageToPlayer2();
        resolveAllTriggers();
        int lifeAfterCombat = gd.playerLifeTotals.get(player2.getId());

        advanceToPlayer2DrawStepObligation();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeAfterCombat - 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        assertThatThrownBy(() -> gs.payDrawStepLifeLoss(gd, player2, asp.getCard().getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No unpaid");
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void payingOneOfTwoObligationsLeavesTheOtherUnpaid() {
        Permanent firstAsp = addReadyAsp();
        firstAsp.setAttacking(true);
        Permanent secondAsp = addReadyAsp();
        secondAsp.setAttacking(true);
        dealCombatDamageToPlayer2();
        resolveAllTriggers();
        int lifeAfterCombat = gd.getLife(player2.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        gs.payDrawStepLifeLoss(gd, player2, firstAsp.getCard().getId());
        advanceToPlayer2DrawStepObligation();

        harness.assertLife(player2, lifeAfterCombat - 1);
    }

    @Test
    void paymentIsTooLateEvenWhileDrawStepTriggerIsOnStack() {
        Permanent asp = addReadyAsp();
        asp.setAttacking(true);
        dealCombatDamageToPlayer2();
        resolveAllTriggers();
        gd.turnNumber = 2;
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.currentStep).isEqualTo(TurnStep.DRAW);
        assertThat(gd.stack).hasSize(1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        assertThatThrownBy(() -> gs.payDrawStepLifeLoss(gd, player2, asp.getCard().getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No unpaid");
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("No obligation is scheduled when Nafs Asp is blocked and deals no damage to a player")
    void blockedCreatesNoObligation() {
        Permanent asp = addReadyAsp();
        asp.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        dealCombatDamageToPlayer2();

        // Blocked: no combat damage reaches player2, so no draw-step obligation is scheduled.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
        assertThat(gd.getDelayedActions(LoseLifeAtNextDrawStepUnlessPays.class)).isEmpty();
    }
}
