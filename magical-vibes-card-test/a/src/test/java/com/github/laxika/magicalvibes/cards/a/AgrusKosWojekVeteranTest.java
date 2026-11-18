package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.cards.c.CourierHawk;
import com.github.laxika.magicalvibes.cards.g.GreaterMossdog;
import com.github.laxika.magicalvibes.cards.v.ViashinoFangtail;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AgrusKosWojekVeteran.class, ViashinoFangtail.class, CourierHawk.class, GreaterMossdog.class})
class AgrusKosWojekVeteranTest extends BaseCardTest {

    @Test
    @DisplayName("Agrus Kos boosts attacking creatures according to their colors")
    void boostsAttackingCreaturesByColor() {
        Permanent agrus = addCreatureReady(player1, new AgrusKosWojekVeteran());
        Permanent red = addCreatureReady(player1, new ViashinoFangtail());
        Permanent white = addCreatureReady(player1, new CourierHawk());
        Permanent green = addCreatureReady(player1, new GreaterMossdog());

        declareAttackers(List.of(0, 1, 2, 3));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, agrus)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, agrus)).isEqualTo(2);
        assertThat(PermanentGrants.powerBoostOf(gd, red)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, red)).isZero();
        assertThat(PermanentGrants.powerBoostOf(gd, white)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, white)).isEqualTo(2);
        assertThat(PermanentGrants.powerBoostOf(gd, green)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, green)).isZero();
    }

    @Test
    @DisplayName("Agrus Kos only boosts attacking creatures")
    void doesNotBoostNonAttackers() {
        addCreatureReady(player1, new AgrusKosWojekVeteran());
        Permanent red = addCreatureReady(player1, new ViashinoFangtail());
        Permanent white = addCreatureReady(player1, new CourierHawk());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, red)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, red)).isZero();
        assertThat(PermanentGrants.powerBoostOf(gd, white)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, white)).isZero();
    }

    @Test
    @DisplayName("Agrus Kos does not trigger when another creature attacks")
    void doesNotTriggerWhenAnotherCreatureAttacks() {
        Permanent agrus = addCreatureReady(player1, new AgrusKosWojekVeteran());
        Permanent red = addCreatureReady(player1, new ViashinoFangtail());

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, agrus)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, agrus)).isZero();
        assertThat(PermanentGrants.powerBoostOf(gd, red)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, red)).isZero();
    }

    @Test
    @DisplayName("Agrus Kos's boosts wear off at end of turn")
    void boostsWearOffAtEndOfTurn() {
        addCreatureReady(player1, new AgrusKosWojekVeteran());
        Permanent red = addCreatureReady(player1, new ViashinoFangtail());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();
        assertThat(PermanentGrants.powerBoostOf(gd, red)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, red)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, red)).isZero();
    }

    @Test
    @DisplayName("Creatures removed from combat before resolution receive no boost")
    void checksAttackingStatusAtResolution() {
        addCreatureReady(player1, new AgrusKosWojekVeteran());
        Permanent red = addCreatureReady(player1, new ViashinoFangtail());
        Permanent white = addCreatureReady(player1, new CourierHawk());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0, 1, 2));
            assertThat(gd.stack).hasSize(1);
            red.setAttacking(false);
            white.setAttacking(false);
            resolveAllTriggers();
        });

        assertThat(PermanentGrants.powerBoostOf(gd, red)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, red)).isZero();
        assertThat(PermanentGrants.powerBoostOf(gd, white)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, white)).isZero();
    }

    @Test
    @DisplayName("Boosts persist after combat and do not extend to later attackers")
    void affectedCreaturesAreFixedAtResolution() {
        Permanent agrus = addCreatureReady(player1, new AgrusKosWojekVeteran());
        Permanent red = addCreatureReady(player1, new ViashinoFangtail());
        Permanent white = addCreatureReady(player1, new CourierHawk());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0, 1));
            resolveAllTriggers();
        });

        agrus.setAttacking(false);
        red.setAttacking(false);
        white.setAttacking(true);

        assertThat(gqs.getEffectivePower(gd, agrus)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, agrus)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, red)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, red)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, white)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, white)).isEqualTo(2);
    }
}
