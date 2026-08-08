package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MawOfTheObzedatTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a creature gives creatures you control +1/+1")
    void boostsOwnCreatures() {
        Permanent maw = harness.addToBattlefieldAndReturn(player1, new MawOfTheObzedat());
        Permanent food = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent survivor = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, food.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(maw.getEffectivePower()).isEqualTo(4);
        assertThat(maw.getEffectiveToughness()).isEqualTo(4);
        assertThat(survivor.getEffectivePower()).isEqualTo(3);
        assertThat(survivor.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not boost the opponent's creatures")
    void doesNotBoostOpponentCreatures() {
        harness.addToBattlefieldAndReturn(player1, new MawOfTheObzedat());
        Permanent food = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent theirs = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, food.getId());
        harness.passBothPriorities();

        assertThat(theirs.getEffectivePower()).isEqualTo(2);
        assertThat(theirs.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Maw can eat itself, still boosting the rest of the team")
    void canSacrificeItself() {
        Permanent maw = harness.addToBattlefieldAndReturn(player1, new MawOfTheObzedat());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, maw.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Maw of the Obzedat");
        assertThat(bears.getEffectivePower()).isEqualTo(3);
        assertThat(bears.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Boost wears off at cleanup")
    void boostWearsOff() {
        Permanent maw = harness.addToBattlefieldAndReturn(player1, new MawOfTheObzedat());
        Permanent food = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, food.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(maw.getEffectivePower()).isEqualTo(3);
        assertThat(maw.getEffectiveToughness()).isEqualTo(3);
    }
}
