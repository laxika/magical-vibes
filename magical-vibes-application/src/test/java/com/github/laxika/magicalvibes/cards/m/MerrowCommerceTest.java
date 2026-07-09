package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MerrowCommerceTest extends BaseCardTest {

    private void advanceToEndStepTrigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to END_STEP → trigger fires
        harness.passBothPriorities(); // resolve the untap trigger
    }

    @Test
    @DisplayName("Untaps all Merfolk you control at the beginning of your end step")
    void untapsControlledMerfolk() {
        harness.addToBattlefield(player1, new MerrowCommerce());
        Permanent merfolk = harness.addToBattlefieldAndReturn(player1, new CoralMerfolk());
        merfolk.tap();

        advanceToEndStepTrigger();

        assertThat(merfolk.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Does not untap non-Merfolk creatures you control")
    void leavesNonMerfolkTapped() {
        harness.addToBattlefield(player1, new MerrowCommerce());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.tap();

        advanceToEndStepTrigger();

        assertThat(bears.isTapped()).isTrue();
    }
}
