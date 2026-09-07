package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FlexibleWaterbender.class, GrizzlyBears.class, Plains.class})
class FlexibleWaterbenderTest extends BaseCardTest {

    @Test
    @DisplayName("Waterbend taps three artifacts or creatures and sets this creature to 5/2")
    void waterbendSetsBasePowerAndToughness() {
        Permanent waterbender = harness.addToBattlefieldAndReturn(player1, new FlexibleWaterbender());
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Plains());

        harness.activateAbility(player1, 0, null, null);

        assertThat(waterbender.isTapped()).isTrue();
        assertThat(firstCreature.isTapped()).isTrue();
        assertThat(secondCreature.isTapped()).isTrue();
        assertThat(land.isTapped()).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, waterbender)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, waterbender)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, waterbender)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, waterbender)).isEqualTo(5);
    }
}
