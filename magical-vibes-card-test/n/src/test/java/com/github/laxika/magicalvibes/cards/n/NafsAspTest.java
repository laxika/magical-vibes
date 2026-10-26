package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HermeticStudy;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.LoseLifeAtNextDrawStepUnlessPays;
import com.github.laxika.magicalvibes.networking.message.GameStateMessage;
import com.github.laxika.magicalvibes.networking.model.DelayedTriggerPaymentView;
import com.github.laxika.magicalvibes.service.JacksonConfig;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NafsAsp.class, GrizzlyBears.class, HermeticStudy.class, Plains.class})
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
        resolveAllTriggers();
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
    @DisplayName("Powerstone mana can pay to avoid the draw-step life loss")
    void powerstoneManaPaysToAvoidLifeLoss() {
        Permanent asp = addReadyAsp();
        asp.setAttacking(true);

        dealCombatDamageToPlayer2();
        resolveAllTriggers();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        gd.playerManaPools.get(player2.getId()).addPowerstoneOnlyColorless(1);
        gs.payDelayedTriggerCost(gd, player2, asp.getCard().getId(), player2.getId());

        assertThat(gd.playerManaPools.get(player2.getId()).getPowerstoneOnlyColorless()).isZero();
        assertThat(gd.getDelayedActions(LoseLifeAtNextDrawStepUnlessPays.class)).isEmpty();
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
        gs.payDelayedTriggerCost(gd, player2, asp.getCard().getId(), player2.getId());

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
        assertThatThrownBy(() -> gs.payDelayedTriggerCost(gd, player2, asp.getCard().getId(), player2.getId()))
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
        gs.payDelayedTriggerCost(gd, player2, firstAsp.getCard().getId(), player2.getId());
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
        assertThatThrownBy(() -> gs.payDelayedTriggerCost(gd, player2, asp.getCard().getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No unpaid");
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void delayedTriggerIsControlledByTheControllerOfTheDamageTrigger() {
        Permanent asp = addReadyAsp();
        asp.setAttacking(true);
        dealCombatDamageToPlayer2();
        resolveAllTriggers();
        gd.turnNumber = 2;
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player1.getId());
        resolveAllTriggers();
        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
    }

    @Test
    void obligationSurvivesTheAspLeavingTheBattlefieldAndDoesNotRepeat() {
        Permanent asp = addReadyAsp();
        asp.setAttacking(true);
        dealCombatDamageToPlayer2();
        resolveAllTriggers();
        gd.playerBattlefields.get(player1.getId()).remove(asp);
        gd.playerGraveyards.get(player1.getId()).add(asp.getCard());

        advanceToPlayer2DrawStepObligation();
        harness.assertLife(player2, 18);
        assertThat(gd.getDelayedActions(LoseLifeAtNextDrawStepUnlessPays.class)).isEmpty();

        advanceToPlayer2DrawStepObligation();
        harness.assertLife(player2, 18);
    }

    @Test
    void damageToTheAspsControllerAlsoCreatesAnObligation() {
        Permanent asp = addReadyAsp();
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HermeticStudy());
        aura.setAttachedTo(asp.getId());
        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();
        resolveAllTriggers();
        harness.assertLife(player1, 19);

        gd.turnNumber = 2;
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
        assertThat(gd.getDelayedActions(LoseLifeAtNextDrawStepUnlessPays.class)).isEmpty();
    }

    /** Player1 passes in their own end step, handing priority to player2 (who owes the payment). */
    private void passToPlayer2InPlayer1EndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passPriority(player1);
    }

    private List<DelayedTriggerPaymentView> offeredPayments(com.github.laxika.magicalvibes.testutil.FakeConnection connection)
            throws Exception {
        harness.publishState();
        String message = connection.getMessagesContaining("\"type\":\"GAME_STATE\"").getLast();
        return new JacksonConfig().objectMapper().readValue(message, GameStateMessage.class).delayedTriggerPayments();
    }

    @Test
    @DisplayName("The damaged player is offered the {1} payment in their game state while holding priority")
    void paymentIsOfferedToTheDamagedPlayerWithPriority() throws Exception {
        Permanent asp = addReadyAsp();
        asp.setAttacking(true);
        dealCombatDamageToPlayer2();
        resolveAllTriggers();
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        passToPlayer2InPlayer1EndStep();

        assertThat(gqs.getPriorityPlayerId(gd)).isEqualTo(player2.getId());
        assertThat(offeredPayments(harness.getConn2())).singleElement().satisfies(payment -> {
            assertThat(payment.sourceCardId()).isEqualTo(asp.getCard().getId());
            assertThat(payment.affectedId()).isEqualTo(player2.getId());
            assertThat(payment.manaCost()).isEqualTo("{1}");
        });
        assertThat(offeredPayments(harness.getConn1())).isEmpty();
    }

    @Test
    @DisplayName("Auto-pass stops before the draw step while the damaged player can pay")
    void autoPassStopsWhileThePaymentIsAffordable() {
        Permanent asp = addReadyAsp();
        asp.setAttacking(true);
        dealCombatDamageToPlayer2();
        resolveAllTriggers();
        harness.addToBattlefield(player2, new Plains());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        gd.playerAutoStopSteps.put(player1.getId(), Set.of());
        gd.playerAutoStopSteps.put(player2.getId(), Set.of());

        passToPlayer2InPlayer1EndStep();

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        assertThat(gqs.getPriorityPlayerId(gd)).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Without mana to pay, auto-pass carries the damaged player past the end step")
    void autoPassDoesNotStopWhenThePaymentIsUnaffordable() {
        Permanent asp = addReadyAsp();
        asp.setAttacking(true);
        dealCombatDamageToPlayer2();
        resolveAllTriggers();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        gd.playerAutoStopSteps.put(player1.getId(), Set.of());
        gd.playerAutoStopSteps.put(player2.getId(), Set.of());

        passToPlayer2InPlayer1EndStep();

        assertThat(gd.currentStep).isNotEqualTo(TurnStep.END_STEP);
    }

    @Test
    @DisplayName("Paying is a special action: the payer keeps priority and an earlier pass no longer counts")
    void payingIsASpecialAction() {
        Permanent asp = addReadyAsp();
        asp.setAttacking(true);
        dealCombatDamageToPlayer2();
        resolveAllTriggers();
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        passToPlayer2InPlayer1EndStep();
        assertThat(gd.priorityPassedBy).containsExactly(player1.getId());

        gs.payDelayedTriggerCost(gd, player2, asp.getCard().getId(), player2.getId());

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        assertThat(gqs.getPriorityPlayerId(gd)).isEqualTo(player2.getId());
        assertThat(gd.priorityPassedBy).isEmpty();
        assertThat(gd.getDelayedActions(LoseLifeAtNextDrawStepUnlessPays.class)).isEmpty();
    }

    @Test
    @DisplayName("Two identical obligations are offered as one payment until both are paid, one at a time")
    void identicalObligationsArePaidOneAtATime() throws Exception {
        NafsAsp aspCard = new NafsAsp();
        LoseLifeAtNextDrawStepUnlessPays obligation =
                new LoseLifeAtNextDrawStepUnlessPays(player2.getId(), 1, 1, aspCard, player1.getId());
        gd.queueDelayedAction(obligation);
        gd.queueDelayedAction(obligation);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        passToPlayer2InPlayer1EndStep();

        assertThat(offeredPayments(harness.getConn2())).hasSize(1);
        gs.payDelayedTriggerCost(gd, player2, aspCard.getId(), player2.getId());

        assertThat(gd.getDelayedActions(LoseLifeAtNextDrawStepUnlessPays.class)).containsExactly(obligation);
        assertThat(offeredPayments(harness.getConn2())).hasSize(1);
        gs.payDelayedTriggerCost(gd, player2, aspCard.getId(), player2.getId());

        assertThat(gd.getDelayedActions(LoseLifeAtNextDrawStepUnlessPays.class)).isEmpty();
        assertThat(offeredPayments(harness.getConn2())).isEmpty();
        assertThat(gd.playerManaPools.get(player2.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Only the damaged player can pay, and only with enough mana")
    void onlyTheDamagedPlayerCanPayWithEnoughMana() {
        Permanent asp = addReadyAsp();
        asp.setAttacking(true);
        dealCombatDamageToPlayer2();
        resolveAllTriggers();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> gs.payDelayedTriggerCost(gd, player1, asp.getCard().getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No unpaid");
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        assertThatThrownBy(() -> gs.payDelayedTriggerCost(gd, player2, asp.getCard().getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(gd.getDelayedActions(LoseLifeAtNextDrawStepUnlessPays.class)).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(1);
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
