package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.cards.n.NyxbornCourser;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GloryBearers.class, NyxbornCourser.class})
class GloryBearersTest extends BaseCardTest {

    @Test
    @DisplayName("Another attacking creature gets +0/+1 until end of turn")
    void anotherAttackingCreatureGetsToughnessBoost() {
        addCreatureReady(player1, new GloryBearers());
        Permanent attacker = addCreatureReady(player1, new NyxbornCourser());

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, attacker)).isEqualTo(0);
        assertThat(PermanentGrants.toughnessBoostOf(gd, attacker)).isEqualTo(1);
    }

    @Test
    @DisplayName("Glory Bearers does not boost itself when it attacks")
    void doesNotBoostItself() {
        Permanent gloryBearers = addCreatureReady(player1, new GloryBearers());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, gloryBearers)).isEqualTo(0);
        assertThat(PermanentGrants.toughnessBoostOf(gd, gloryBearers)).isEqualTo(0);
    }

    @Test
    @DisplayName("Each other attacking creature gets its own boost")
    void boostsEachOtherAttacker() {
        addCreatureReady(player1, new GloryBearers());
        Permanent firstAttacker = addCreatureReady(player1, new NyxbornCourser());
        Permanent secondAttacker = addCreatureReady(player1, new NyxbornCourser());

        declareAttackers(player1, List.of(1, 2));
        resolveAllTriggers();

        assertThat(PermanentGrants.toughnessBoostOf(gd, firstAttacker)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, secondAttacker)).isEqualTo(1);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new GloryBearers());
        Permanent attacker = addCreatureReady(player1, new NyxbornCourser());

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();
        assertThat(PermanentGrants.toughnessBoostOf(gd, attacker)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, attacker)).isEqualTo(0);
        assertThat(PermanentGrants.toughnessBoostOf(gd, attacker)).isEqualTo(0);
    }

    @Test
    @DisplayName("Opposing attackers do not receive a boost")
    void doesNotBoostOpposingAttacker() {
        Permanent gloryBearers = addCreatureReady(player1, new GloryBearers());
        Permanent attacker = addCreatureReady(player2, new NyxbornCourser());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, attacker)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, attacker)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, gloryBearers)).isZero();
    }

    @Test
    @DisplayName("Nonattacking creatures do not receive a boost")
    void doesNotBoostNonattackingCreature() {
        addCreatureReady(player1, new GloryBearers());
        Permanent attacker = addCreatureReady(player1, new NyxbornCourser());
        Permanent nonattacker = addCreatureReady(player1, new NyxbornCourser());

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(PermanentGrants.toughnessBoostOf(gd, attacker)).isEqualTo(1);
        assertThat(PermanentGrants.powerBoostOf(gd, nonattacker)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, nonattacker)).isZero();
    }

    @Test
    @DisplayName("Two attacking Glory Bearers boost each other and both boost another attacker")
    void multipleGloryBearersBoostOtherAttackers() {
        Permanent first = addCreatureReady(player1, new GloryBearers());
        Permanent second = addCreatureReady(player1, new GloryBearers());
        Permanent attacker = addCreatureReady(player1, new NyxbornCourser());

        declareAttackers(player1, List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(PermanentGrants.toughnessBoostOf(gd, first)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, second)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, attacker)).isEqualTo(2);
        assertThat(PermanentGrants.powerBoostOf(gd, first)).isZero();
        assertThat(PermanentGrants.powerBoostOf(gd, second)).isZero();
        assertThat(PermanentGrants.powerBoostOf(gd, attacker)).isZero();
    }
}
