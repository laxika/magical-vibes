package com.github.laxika.magicalvibes.cards.r;

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

        assertThat(ogre.getPowerModifier()).isEqualTo(3);
        assertThat(ogre.getToughnessModifier()).isZero();
    }

    @Test
    void attackingWithAnotherCreatureDoesNotTrigger() {
        Permanent ogre = addCreatureReady(player1, new RecklessOgre());
        addCreatureReady(player1, new RecklessOgre());

        declareAttackers(player1, List.of(0, 1));
        harness.passBothPriorities();

        assertThat(ogre.getPowerModifier()).isZero();
        assertThat(ogre.getToughnessModifier()).isZero();
    }

    @Test
    void attackingAloneWithAnotherOgreStayingBackBoostsOnlyAttacker() {
        Permanent attacker = addCreatureReady(player1, new RecklessOgre());
        Permanent nonattacker = addCreatureReady(player1, new RecklessOgre());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(attacker.getPowerModifier()).isEqualTo(3);
        assertThat(attacker.getToughnessModifier()).isZero();
        assertThat(nonattacker.getPowerModifier()).isZero();
        assertThat(nonattacker.getToughnessModifier()).isZero();
    }

    @Test
    void opponentAttackingAloneBoostsOnlyTheirOgre() {
        Permanent defendingOgre = addCreatureReady(player1, new RecklessOgre());
        Permanent attackingOgre = addCreatureReady(player2, new RecklessOgre());

        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();

        assertThat(attackingOgre.getPowerModifier()).isEqualTo(3);
        assertThat(attackingOgre.getToughnessModifier()).isZero();
        assertThat(defendingOgre.getPowerModifier()).isZero();
        assertThat(defendingOgre.getToughnessModifier()).isZero();
    }

    @Test
    void boostWearsOffAtEndOfTurn() {
        Permanent ogre = addCreatureReady(player1, new RecklessOgre());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        assertThat(ogre.getPowerModifier()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(ogre.getPowerModifier()).isZero();
        assertThat(ogre.getToughnessModifier()).isZero();
    }
}
