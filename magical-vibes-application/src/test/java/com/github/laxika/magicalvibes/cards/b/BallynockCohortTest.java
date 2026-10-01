package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.ArmoredAscension;
import com.github.laxika.magicalvibes.cards.s.Somnomancer;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BallynockCohort.class, BoggartRamGang.class, ArmoredAscension.class, Somnomancer.class})
class BallynockCohortTest extends BaseCardTest {

    @Test
    @DisplayName("Base 2/2 when no other white creature is controlled")
    void noBoostWhenAlone() {
        Permanent cohort = addCreatureReady(player1, new BallynockCohort());

        assertThat(gqs.getEffectivePower(gd, cohort)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, cohort)).isEqualTo(2);
    }

    @Test
    @DisplayName("No boost with a non-white creature")
    void noBoostWithNonWhiteCreature() {
        Permanent cohort = addCreatureReady(player1, new BallynockCohort());
        harness.addToBattlefield(player1, new BoggartRamGang());

        assertThat(gqs.getEffectivePower(gd, cohort)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, cohort)).isEqualTo(2);
    }

    @Test
    @DisplayName("Gets +1/+1 when controlling another white creature")
    void boostWithAnotherWhiteCreature() {
        Permanent cohort = addCreatureReady(player1, new BallynockCohort());
        harness.addToBattlefield(player1, new BallynockCohort());

        assertThat(gqs.getEffectivePower(gd, cohort)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, cohort)).isEqualTo(3);
    }

    @Test
    @DisplayName("Self does not count — a lone Cohort is not 'another white creature'")
    void selfDoesNotCount() {
        Permanent cohort = addCreatureReady(player1, new BallynockCohort());

        assertThat(gqs.getEffectivePower(gd, cohort)).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent's white creature does not grant the boost")
    void opponentWhiteCreatureDoesNotCount() {
        Permanent cohort = addCreatureReady(player1, new BallynockCohort());
        harness.addToBattlefield(player2, new BallynockCohort());

        assertThat(gqs.getEffectivePower(gd, cohort)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, cohort)).isEqualTo(2);
    }

    @Test
    @DisplayName("Loses boost when the other white creature leaves the battlefield")
    void losesBoostWhenWhiteCreatureLeaves() {
        Permanent cohort = addCreatureReady(player1, new BallynockCohort());
        Permanent otherCohort = harness.addToBattlefieldAndReturn(player1, new BallynockCohort());

        assertThat(gqs.getEffectivePower(gd, cohort)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId()).remove(otherCohort);

        assertThat(gqs.getEffectivePower(gd, cohort)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, cohort)).isEqualTo(2);
    }

    @Test
    @DisplayName("A white noncreature does not grant the boost")
    void noBoostWithWhiteNonCreature() {
        Permanent cohort = addCreatureReady(player1, new BallynockCohort());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ArmoredAscension());
        aura.setAttachedTo(cohort.getId());

        assertThat(gqs.getEffectivePower(gd, cohort)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, cohort)).isEqualTo(2);
    }

    @Test
    @DisplayName("First strike kills a 2/1 before it deals combat damage")
    void firstStrikeDealsCombatDamageBeforeRegularDamage() {
        Permanent blocker = addCreatureReady(player1, new BallynockCohort());
        Permanent attacker = addCreatureReady(player2, new Somnomancer());
        attacker.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Ballynock Cohort");
        harness.assertInGraveyard(player2, "Somnomancer");
    }
}
