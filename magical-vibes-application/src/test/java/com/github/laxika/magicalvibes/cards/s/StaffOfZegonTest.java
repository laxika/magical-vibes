package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StaffOfZegon.class, GrizzlyBears.class, Forest.class})
class StaffOfZegonTest extends BaseCardTest {

    @Test
    @DisplayName("Taps to give target creature -2/-0 until end of turn")
    void givesTargetCreatureNegativePowerUntilEndOfTurn() {
        addStaffAndTarget();
        Permanent bears = findPermanent(player2, "Grizzly Bears");

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Staff of Zegon").isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void rejectsNonCreatureTarget() {
        addStaffAndTarget();
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addStaffAndTarget() {
        harness.addToBattlefield(player1, new StaffOfZegon());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }
}
