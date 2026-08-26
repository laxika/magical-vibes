package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.LoseLifeAtNextDrawStepUnlessPays;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GlassAsp.class, GrizzlyBears.class})
class GlassAspTest extends BaseCardTest {

    private Permanent addReadyAsp() {
        return addCreatureReady(player1, new GlassAsp());
    }

    private void dealCombatDamageToPlayer2() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private void advanceToPlayer2DrawStepObligation() {
        gd.turnNumber = 2;
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Declining to pay {2} loses 2 life at the damaged player's next draw step")
    void declineLosesLife() {
        Permanent asp = addReadyAsp();
        asp.setAttacking(true);

        dealCombatDamageToPlayer2();
        int lifeAfterCombat = gd.playerLifeTotals.get(player2.getId());

        advanceToPlayer2DrawStepObligation();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeAfterCombat - 2);
    }

    @Test
    @DisplayName("Paying {2} before the draw step avoids the life loss")
    void payAvoidsLife() {
        Permanent asp = addReadyAsp();
        asp.setAttacking(true);

        dealCombatDamageToPlayer2();
        int lifeAfterCombat = gd.playerLifeTotals.get(player2.getId());

        advanceToPlayer2DrawStepObligation();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeAfterCombat);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.WHITE)).isZero();
    }

    @Test
    @DisplayName("No obligation is scheduled when Glass Asp is blocked and deals no combat damage")
    void blockedCreatesNoObligation() {
        Permanent asp = addReadyAsp();
        asp.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        dealCombatDamageToPlayer2();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
        assertThat(gd.getDelayedActions(LoseLifeAtNextDrawStepUnlessPays.class)).isEmpty();
    }
}
