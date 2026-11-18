package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RecklessOgre.class})
class RecklessOgreTest extends BaseCardTest {

    @Test
    void attackingAloneGetsPlusThreePower() {
        Permanent ogre = addCreatureReady(player1, new RecklessOgre());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, ogre)).isEqualTo(3);
        assertThat(PermanentGrants.toughnessBoostOf(gd, ogre)).isZero();
    }

    @Test
    void attackingWithAnotherCreatureDoesNotTrigger() {
        Permanent ogre = addCreatureReady(player1, new RecklessOgre());
        addCreatureReady(player1, new RecklessOgre());

        declareAttackers(player1, List.of(0, 1));
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, ogre)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, ogre)).isZero();
    }

    @Test
    void attackingAloneWithAnotherOgreStayingBackBoostsOnlyAttacker() {
        Permanent attacker = addCreatureReady(player1, new RecklessOgre());
        Permanent nonattacker = addCreatureReady(player1, new RecklessOgre());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, attacker)).isEqualTo(3);
        assertThat(PermanentGrants.toughnessBoostOf(gd, attacker)).isZero();
        assertThat(PermanentGrants.powerBoostOf(gd, nonattacker)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, nonattacker)).isZero();
    }

    @Test
    void opponentAttackingAloneBoostsOnlyTheirOgre() {
        Permanent defendingOgre = addCreatureReady(player1, new RecklessOgre());
        Permanent attackingOgre = addCreatureReady(player2, new RecklessOgre());

        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, attackingOgre)).isEqualTo(3);
        assertThat(PermanentGrants.toughnessBoostOf(gd, attackingOgre)).isZero();
        assertThat(PermanentGrants.powerBoostOf(gd, defendingOgre)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, defendingOgre)).isZero();
    }

    @Test
    void boostWearsOffAtEndOfTurn() {
        Permanent ogre = addCreatureReady(player1, new RecklessOgre());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        assertThat(PermanentGrants.powerBoostOf(gd, ogre)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, ogre)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, ogre)).isZero();
    }
}
