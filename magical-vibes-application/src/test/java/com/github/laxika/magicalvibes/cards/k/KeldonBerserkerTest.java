package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KeldonBerserkerTest extends BaseCardTest {

    @Test
    void getsPlusThreePowerWhenAttackingWithNoUntappedLands() {
        Permanent berserker = addCreatureReady(player1, new KeldonBerserker());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(berserker.getPowerModifier()).isEqualTo(3);
        assertThat(berserker.getToughnessModifier()).isZero();
    }

    @Test
    void doesNotTriggerWhileControllingAnUntappedLand() {
        Permanent berserker = addCreatureReady(player1, new KeldonBerserker());
        harness.addToBattlefield(player1, new Forest());

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).isEmpty();
        assertThat(berserker.getPowerModifier()).isZero();
    }

    @Test
    void triggersWhenAllControlledLandsAreTapped() {
        Permanent berserker = addCreatureReady(player1, new KeldonBerserker());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        forest.tap();

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(berserker.getPowerModifier()).isEqualTo(3);
    }

    @Test
    void boostWearsOffAtEndOfTurn() {
        Permanent berserker = addCreatureReady(player1, new KeldonBerserker());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        assertThat(berserker.getPowerModifier()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(berserker.getPowerModifier()).isZero();
        assertThat(berserker.getToughnessModifier()).isZero();
    }
}
