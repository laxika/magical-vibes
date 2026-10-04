package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BenalishCavalry;
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

@CardUsed({GlassAsp.class, BenalishCavalry.class})
class GlassAspTest extends BaseCardTest {

    private Permanent addReadyAsp() {
        return addCreatureReady(player1, new GlassAsp());
    }

    private void dealCombatDamageToPlayer2() {
        resolveCombat();
        resolveAllTriggers();
    }

    private void advanceToPlayer2DrawStepObligation() {
        advanceToPlayer2DrawStep();
        resolveAllTriggers();
    }

    private void advanceToPlayer2DrawStep() {
        gd.turnNumber = 2;
        advanceToUpkeep(player2);
        harness.passUntil(player2, TurnStep.DRAW);
    }

    @Test
    @DisplayName("An unpaid obligation loses 2 life at the damaged player's next draw step")
    void unpaidObligationLosesLife() {
        Permanent asp = addReadyAsp();
        asp.setAttacking(true);

        dealCombatDamageToPlayer2();
        int lifeAfterCombat = gd.playerLifeTotals.get(player2.getId());

        advanceToPlayer2DrawStepObligation();

        assertThat(gd.interaction.activeInteraction()).isNull();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeAfterCombat - 2);
    }

    @Test
    @DisplayName("Paying {2} before the draw step avoids the life loss")
    void payAvoidsLife() {
        Permanent asp = addReadyAsp();
        asp.setAttacking(true);

        dealCombatDamageToPlayer2();
        int lifeAfterCombat = gd.playerLifeTotals.get(player2.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.WHITE, 2);
        gs.payDrawStepLifeLoss(gd, player2, asp.getCard().getId());

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.getDelayedActions(LoseLifeAtNextDrawStepUnlessPays.class)).isEmpty();
        assertThat(gd.stack).isEmpty();

        advanceToPlayer2DrawStepObligation();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeAfterCombat);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.WHITE)).isZero();
    }

    @Test
    @DisplayName("Each Glass Asp that deals combat damage creates a separate obligation")
    void eachAspCreatesSeparateObligation() {
        Permanent firstAsp = addReadyAsp();
        firstAsp.setAttacking(true);
        Permanent secondAsp = addReadyAsp();
        secondAsp.setAttacking(true);

        dealCombatDamageToPlayer2();
        int lifeAfterCombat = gd.playerLifeTotals.get(player2.getId());

        advanceToPlayer2DrawStepObligation();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeAfterCombat - 4);
    }

    @Test
    @DisplayName("No obligation is scheduled when Glass Asp is blocked and deals no combat damage")
    void blockedCreatesNoObligation() {
        Permanent asp = addReadyAsp();
        asp.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new BenalishCavalry());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        dealCombatDamageToPlayer2();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
        assertThat(gd.getDelayedActions(LoseLifeAtNextDrawStepUnlessPays.class)).isEmpty();
    }

    @Test
    @DisplayName("The delayed ability remains controlled by the original trigger's controller")
    void delayedAbilityHasOriginalController() {
        Permanent asp = addReadyAsp();
        asp.setAttacking(true);
        dealCombatDamageToPlayer2();
        int lifeAfterCombat = gd.playerLifeTotals.get(player2.getId());

        advanceToPlayer2DrawStep();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getLast().getControllerId()).isEqualTo(player1.getId());
        resolveAllTriggers();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeAfterCombat - 2);
    }

    @Test
    @DisplayName("Paying for one Asp does not discharge another Asp's obligation")
    void paymentDischargesOnlyOneObligation() {
        Permanent firstAsp = addReadyAsp();
        firstAsp.setAttacking(true);
        Permanent secondAsp = addReadyAsp();
        secondAsp.setAttacking(true);
        dealCombatDamageToPlayer2();
        int lifeAfterCombat = gd.playerLifeTotals.get(player2.getId());

        gd.turnNumber = 2;
        advanceToUpkeep(player2);
        harness.addMana(player2, ManaColor.WHITE, 2);
        gs.payDrawStepLifeLoss(gd, player2, firstAsp.getCard().getId());

        harness.passUntil(player2, TurnStep.DRAW);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeAfterCombat - 2);
    }

    @Test
    @DisplayName("Payment is too late once the next draw step begins")
    void cannotPayInResponseToDrawStepTrigger() {
        Permanent asp = addReadyAsp();
        asp.setAttacking(true);
        dealCombatDamageToPlayer2();
        int lifeAfterCombat = gd.playerLifeTotals.get(player2.getId());

        advanceToPlayer2DrawStep();
        harness.addMana(player2, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> gs.payDrawStepLifeLoss(gd, player2, asp.getCard().getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.WHITE)).isEqualTo(2);
        resolveAllTriggers();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeAfterCombat - 2);
    }

    @Test
    @DisplayName("The delayed life loss persists after Glass Asp leaves the battlefield")
    void obligationPersistsWithoutSource() {
        Permanent asp = addReadyAsp();
        asp.setAttacking(true);
        dealCombatDamageToPlayer2();
        int lifeAfterCombat = gd.playerLifeTotals.get(player2.getId());
        gd.playerBattlefields.get(player1.getId()).remove(asp);
        gd.playerGraveyards.get(player1.getId()).add(asp.getCard());

        advanceToPlayer2DrawStepObligation();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeAfterCombat - 2);
    }
}
