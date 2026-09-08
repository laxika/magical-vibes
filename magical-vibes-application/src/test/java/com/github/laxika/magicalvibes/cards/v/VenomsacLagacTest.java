package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VenomsacLagacTest extends BaseCardTest {

    @Test
    @DisplayName("Saddle 2 taps another creature and saddles Venomsac Lagac")
    void saddleTapsAnotherCreature() {
        Permanent lagac = addCreatureReady(player1, new VenomsacLagac());
        Permanent helper = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(lagac.isSaddled()).isTrue();
        assertThat(helper.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Attacking while saddled gives Venomsac Lagac +0/+3 until end of turn")
    void attacksWhileSaddled() {
        Permanent lagac = addCreatureReady(player1, new VenomsacLagac());
        addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, lagac)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, lagac)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(lagac.isSaddled()).isFalse();
        assertThat(gqs.getEffectivePower(gd, lagac)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, lagac)).isEqualTo(1);
    }

    @Test
    @DisplayName("Attacking while not saddled does not trigger")
    void doesNotTriggerWhenNotSaddled() {
        Permanent lagac = addCreatureReady(player1, new VenomsacLagac());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, lagac)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, lagac)).isEqualTo(1);
    }
}
