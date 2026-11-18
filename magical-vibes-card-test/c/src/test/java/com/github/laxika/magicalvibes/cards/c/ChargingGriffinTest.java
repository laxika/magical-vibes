package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChargingGriffin.class})
class ChargingGriffinTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+1 until end of turn when it attacks")
    void boostsOnAttack() {
        Permanent griffin = addCreatureReady(player1, new ChargingGriffin());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, griffin)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, griffin)).isEqualTo(1);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        Permanent griffin = addCreatureReady(player1, new ChargingGriffin());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, griffin)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, griffin)).isEqualTo(0);
        assertThat(PermanentGrants.toughnessBoostOf(gd, griffin)).isEqualTo(0);
    }

    @Test
    @DisplayName("No boost when it does not attack")
    void noBoostWithoutAttacking() {
        Permanent griffin = addCreatureReady(player1, new ChargingGriffin());

        declareAttackers(player1, List.of());
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, griffin)).isEqualTo(0);
        assertThat(PermanentGrants.toughnessBoostOf(gd, griffin)).isEqualTo(0);
    }

    @Test
    @DisplayName("Attack boost waits for resolution and affects only the attacking Griffin")
    void boostResolvesOnlyForItsSource() {
        Permanent attacker = addCreatureReady(player1, new ChargingGriffin());
        Permanent nonattacker = addCreatureReady(player1, new ChargingGriffin());
        Permanent defender = addCreatureReady(player2, new ChargingGriffin());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(0)));

        assertThat(gd.stack).hasSize(1);
        assertThat(PermanentGrants.powerBoostOf(gd, attacker)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, attacker)).isZero();

        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, attacker)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, attacker)).isEqualTo(1);
        assertThat(PermanentGrants.powerBoostOf(gd, nonattacker)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, nonattacker)).isZero();
        assertThat(PermanentGrants.powerBoostOf(gd, defender)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, defender)).isZero();
    }

    @Test
    @DisplayName("Each attacking Griffin gets its own boost")
    void multipleAttackersBoostIndependently() {
        Permanent first = addCreatureReady(player1, new ChargingGriffin());
        Permanent second = addCreatureReady(player1, new ChargingGriffin());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, first)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, first)).isEqualTo(1);
        assertThat(PermanentGrants.powerBoostOf(gd, second)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, second)).isEqualTo(1);
    }

    @Test
    @DisplayName("Attack boost works for the other player")
    void boostsWhenOpponentAttacks() {
        Permanent griffin = addCreatureReady(player2, new ChargingGriffin());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, griffin)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, griffin)).isEqualTo(1);
    }
}
