package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(RecklessPangolin.class)
class RecklessPangolinTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+1 until end of turn when it attacks")
    void boostsOnAttack() {
        Permanent pangolin = addCreatureReady(player1, new RecklessPangolin());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, pangolin)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, pangolin)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not get the boost without attacking")
    void noBoostWithoutAttacking() {
        Permanent pangolin = addCreatureReady(player1, new RecklessPangolin());

        assertThat(PermanentGrants.powerBoostOf(gd, pangolin)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, pangolin)).isZero();
    }

    @Test
    @DisplayName("The attack boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent pangolin = addCreatureReady(player1, new RecklessPangolin());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        assertThat(PermanentGrants.powerBoostOf(gd, pangolin)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, pangolin)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, pangolin)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, pangolin)).isZero();
    }

    @Test
    @DisplayName("Attack bonus is applied when the trigger resolves, not when attackers are declared")
    void boostWaitsForTriggerResolution() {
        Permanent pangolin = addCreatureReady(player1, new RecklessPangolin());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(0)));

        assertThat(gd.stack).hasSize(1);
        assertThat(PermanentGrants.powerBoostOf(gd, pangolin)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, pangolin)).isZero();

        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, pangolin)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, pangolin)).isEqualTo(1);
    }

    @Test
    @DisplayName("Only the attacking Pangolin gets the bonus")
    void boostAffectsOnlyItsSource() {
        Permanent attacker = addCreatureReady(player1, new RecklessPangolin());
        Permanent nonattacker = addCreatureReady(player1, new RecklessPangolin());
        Permanent opponent = addCreatureReady(player2, new RecklessPangolin());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, attacker)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, attacker)).isEqualTo(1);
        assertThat(PermanentGrants.powerBoostOf(gd, nonattacker)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, nonattacker)).isZero();
        assertThat(PermanentGrants.powerBoostOf(gd, opponent)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, opponent)).isZero();
    }

}
