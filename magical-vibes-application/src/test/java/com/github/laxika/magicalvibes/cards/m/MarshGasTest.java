package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MarshGas.class, GrizzlyBears.class})
class MarshGasTest extends BaseCardTest {

    @Test
    @DisplayName("Gives -2/-0 to every creature on both battlefields")
    void debuffsAllCreaturesPowerOnly() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent theirs = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.castFromHand(player1, new MarshGas(), "{B}");
        harness.passBothPriorities();

        assertThat(own.getEffectivePower()).isEqualTo(0);
        assertThat(own.getEffectiveToughness()).isEqualTo(2);
        assertThat(theirs.getEffectivePower()).isEqualTo(0);
        assertThat(theirs.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Effect wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.castFromHand(player1, new MarshGas(), "{B}");
        harness.passBothPriorities();

        assertThat(bears.getEffectivePower()).isEqualTo(0);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getEffectivePower()).isEqualTo(2);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not affect creatures entering after it resolves")
    void doesNotAffectCreaturesEnteringAfterResolution() {
        harness.castFromHand(player1, new MarshGas(), "{B}");
        harness.passBothPriorities();

        Permanent lateCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(lateCreature.getEffectivePower()).isEqualTo(2);
        assertThat(lateCreature.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Affects creatures that enter while the spell is on the stack")
    void affectsCreaturesEnteringBeforeResolution() {
        harness.castFromHand(player1, new MarshGas(), "{B}");
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.passBothPriorities();

        assertThat(bears.getEffectivePower()).isEqualTo(0);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Multiple copies cumulatively reduce power below zero without reducing toughness")
    void multipleCopiesStackAndExpireTogether() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.castFromHand(player1, new MarshGas(), "{B}");
        harness.passBothPriorities();
        harness.castFromHand(player1, new MarshGas(), "{B}");
        harness.passBothPriorities();

        assertThat(bears.getEffectivePower()).isEqualTo(-2);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bears);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getEffectivePower()).isEqualTo(2);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
    }
}
