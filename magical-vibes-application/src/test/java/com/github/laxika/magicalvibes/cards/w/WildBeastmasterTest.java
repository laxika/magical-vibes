package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WildBeastmasterTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking boosts other creatures you control by the Beastmaster's power")
    void attackBoostsOtherOwnCreatures() {
        Permanent beastmaster = addCreatureReady(player1, new WildBeastmaster());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, beastmaster)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, beastmaster)).isEqualTo(1);
    }

    @Test
    @DisplayName("X scales with the Beastmaster's current power and skips opponents' creatures")
    void boostScalesWithPowerAndSkipsOpponents() {
        Permanent beastmaster = addCreatureReady(player1, new WildBeastmaster());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent enemyBears = addCreatureReady(player2, new GrizzlyBears());
        beastmaster.setPowerModifier(2);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, enemyBears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        addCreatureReady(player1, new WildBeastmaster());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }
}
