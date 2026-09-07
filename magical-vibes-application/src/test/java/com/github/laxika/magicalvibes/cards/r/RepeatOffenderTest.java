package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(RepeatOffender.class)
class RepeatOffenderTest extends BaseCardTest {

    @Test
    @DisplayName("The first activation suspects Repeat Offender")
    void firstActivationSuspectsCreature() {
        Permanent repeatOffender = addReadyRepeatOffender();

        activate();

        assertThat(repeatOffender.isSuspected()).isTrue();
        assertThat(repeatOffender.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A later activation puts a +1/+1 counter on a suspected Repeat Offender")
    void laterActivationAddsCounter() {
        Permanent repeatOffender = addReadyRepeatOffender();

        activate();
        activate();

        assertThat(repeatOffender.isSuspected()).isTrue();
        assertThat(repeatOffender.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private Permanent addReadyRepeatOffender() {
        Permanent repeatOffender = harness.addToBattlefieldAndReturn(player1, new RepeatOffender());
        repeatOffender.setSummoningSick(false);
        return repeatOffender;
    }

    private void activate() {
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }
}
