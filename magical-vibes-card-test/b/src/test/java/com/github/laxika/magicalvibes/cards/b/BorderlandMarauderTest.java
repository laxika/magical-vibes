package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BorderlandMarauder.class})
class BorderlandMarauderTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking gives Borderland Marauder +2/+0")
    void attackTriggerBoostsPower() {
        Permanent marauder = addCreatureReady(player1, new BorderlandMarauder());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, marauder)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, marauder)).isZero();
        assertThat(gqs.getEffectivePower(gd, marauder)).isEqualTo(3);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOff() {
        Permanent marauder = addCreatureReady(player1, new BorderlandMarauder());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, marauder)).isZero();
    }

    @Test
    @DisplayName("No boost while Borderland Marauder stays back")
    void noBoostWithoutAttacking() {
        Permanent marauder = addCreatureReady(player1, new BorderlandMarauder());

        declareAttackers(player1, List.of());
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, marauder)).isZero();
    }

    @Test
    @DisplayName("Attack boost uses the stack and affects only the attacking copy")
    void attackBoostResolvesOnlyForItsSource() {
        Permanent attacker = addCreatureReady(player1, new BorderlandMarauder());
        Permanent stayingBack = addCreatureReady(player1, new BorderlandMarauder());
        Permanent opponent = addCreatureReady(player2, new BorderlandMarauder());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(0)));

        assertThat(gd.stack).hasSize(1);
        assertThat(PermanentGrants.powerBoostOf(gd, attacker)).isZero();

        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, attacker)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, attacker)).isZero();
        assertThat(PermanentGrants.powerBoostOf(gd, stayingBack)).isZero();
        assertThat(PermanentGrants.powerBoostOf(gd, opponent)).isZero();
    }
}
