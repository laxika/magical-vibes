package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RavenousSkirgeTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +2/+0 until end of turn when it attacks")
    void boostsOnAttack() {
        Permanent skirge = addCreatureReady(player1, new RavenousSkirge());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(skirge.getPowerModifier()).isEqualTo(2);
        assertThat(skirge.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        Permanent skirge = addCreatureReady(player1, new RavenousSkirge());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(skirge.getPowerModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(skirge.getPowerModifier()).isEqualTo(0);
    }
}
