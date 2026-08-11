package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PackMastiffTest extends BaseCardTest {

    @Test
    @DisplayName("Activation boosts each Pack Mastiff you control")
    void boostsPackMastiffsYouControl() {
        Permanent mastiff = addCreatureReady(player1, new PackMastiff());
        Permanent otherMastiff = addCreatureReady(player1, new PackMastiff());
        Permanent ownBears = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentMastiff = addCreatureReady(player2, new PackMastiff());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mastiff)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, otherMastiff)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mastiff)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, ownBears)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentMastiff)).isEqualTo(2);
    }

    @Test
    @DisplayName("Activation boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent mastiff = addCreatureReady(player1, new PackMastiff());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mastiff)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mastiff)).isEqualTo(2);
    }
}
