package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GracefulCat.class})
class GracefulCatTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+1 until end of turn when it attacks")
    void boostsOnAttack() {
        Permanent cat = addCreatureReady(player1, new GracefulCat());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, cat)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, cat)).isEqualTo(1);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        Permanent cat = addCreatureReady(player1, new GracefulCat());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, cat)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, cat)).isEqualTo(0);
        assertThat(PermanentGrants.toughnessBoostOf(gd, cat)).isEqualTo(0);
    }

    @Test
    @DisplayName("No boost when it does not attack")
    void noBoostWithoutAttacking() {
        Permanent cat = addCreatureReady(player1, new GracefulCat());

        declareAttackers(player1, List.of());
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, cat)).isEqualTo(0);
        assertThat(PermanentGrants.toughnessBoostOf(gd, cat)).isEqualTo(0);
    }

    @Test
    @DisplayName("Attack boost waits for its triggered ability to resolve")
    void boostWaitsForResolution() {
        Permanent cat = addCreatureReady(player1, new GracefulCat());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(0)));

        assertThat(gd.stack).hasSize(1);
        assertThat(PermanentGrants.powerBoostOf(gd, cat)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, cat)).isZero();

        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, cat)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, cat)).isEqualTo(1);
    }

    @Test
    @DisplayName("Only the attacking Cat receives its boost")
    void onlyAttackingCatIsBoosted() {
        Permanent attacker = addCreatureReady(player1, new GracefulCat());
        Permanent otherCat = addCreatureReady(player1, new GracefulCat());
        Permanent opposingCat = addCreatureReady(player2, new GracefulCat());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, attacker)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, attacker)).isEqualTo(1);
        assertThat(PermanentGrants.powerBoostOf(gd, otherCat)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, otherCat)).isZero();
        assertThat(PermanentGrants.powerBoostOf(gd, opposingCat)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, opposingCat)).isZero();
    }

    @Test
    @DisplayName("Each attacking Cat receives exactly its own boost")
    void multipleAttackingCatsBoostIndependently() {
        Permanent first = addCreatureReady(player1, new GracefulCat());
        Permanent second = addCreatureReady(player1, new GracefulCat());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, first)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, first)).isEqualTo(1);
        assertThat(PermanentGrants.powerBoostOf(gd, second)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, second)).isEqualTo(1);
    }
}
