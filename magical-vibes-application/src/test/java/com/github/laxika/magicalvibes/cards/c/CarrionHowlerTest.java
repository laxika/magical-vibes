package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CarrionHowler.class})
class CarrionHowlerTest extends BaseCardTest {

    @Test
    void payingLifeBoostsCarrionHowler() {
        Permanent howler = harness.addToBattlefieldAndReturn(player1, new CarrionHowler());
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 1);
        assertThat(gqs.getEffectivePower(gd, howler)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, howler)).isEqualTo(1);
    }

    @Test
    void boostWearsOffAtEndOfTurn() {
        Permanent howler = harness.addToBattlefieldAndReturn(player1, new CarrionHowler());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, howler)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, howler)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, howler)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, howler)).isEqualTo(2);
    }
}
