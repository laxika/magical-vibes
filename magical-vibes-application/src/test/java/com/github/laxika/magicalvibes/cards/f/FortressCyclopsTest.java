package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FortressCyclopsTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking gives Fortress Cyclops +3/+0 until end of turn")
    void attackTriggerBoostsPower() {
        Permanent cyclops = addCreatureReady(player1, new FortressCyclops());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(cyclops.getPowerModifier()).isEqualTo(3);
        assertThat(cyclops.getToughnessModifier()).isZero();
        assertThat(gqs.getEffectivePower(gd, cyclops)).isEqualTo(6);
    }

    @Test
    @DisplayName("The attack boost wears off at end of turn")
    void attackBoostWearsOff() {
        Permanent cyclops = addCreatureReady(player1, new FortressCyclops());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities(); // END_STEP -> CLEANUP

        assertThat(cyclops.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("Blocking gives Fortress Cyclops +0/+3 until end of turn")
    void blockTriggerBoostsToughness() {
        Permanent attacker = addCreatureReady(player1, new GiantSpider());
        attacker.setAttacking(true);
        Permanent cyclops = addCreatureReady(player2, new FortressCyclops());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(cyclops.getPowerModifier()).isZero();
        assertThat(cyclops.getToughnessModifier()).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, cyclops)).isEqualTo(6);
    }

    @Test
    @DisplayName("No boost when Fortress Cyclops neither attacks nor blocks")
    void noBoostWithoutCombat() {
        Permanent attacker = addCreatureReady(player1, new GiantSpider());
        attacker.setAttacking(true);
        Permanent cyclops = addCreatureReady(player2, new FortressCyclops());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        assertThat(cyclops.getPowerModifier()).isZero();
        assertThat(cyclops.getToughnessModifier()).isZero();
    }
}
