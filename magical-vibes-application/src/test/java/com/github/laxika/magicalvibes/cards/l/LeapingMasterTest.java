package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LeapingMasterTest extends BaseCardTest {

    @Test
    @DisplayName("Activating grants flying until end of turn")
    void grantsFlying() {
        Permanent leapingMaster = harness.addToBattlefieldAndReturn(player1, new LeapingMaster());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, leapingMaster, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Granted flying wears off at end of turn")
    void flyingWearsOffAtEndOfTurn() {
        Permanent leapingMaster = harness.addToBattlefieldAndReturn(player1, new LeapingMaster());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, leapingMaster, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, leapingMaster, Keyword.FLYING)).isFalse();
    }
}
