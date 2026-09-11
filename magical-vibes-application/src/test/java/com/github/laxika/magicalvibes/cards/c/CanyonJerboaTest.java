package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CanyonJerboa.class, Forest.class, GrizzlyBears.class})
class CanyonJerboaTest extends BaseCardTest {

    @Test
    void landfallBoostsAllCreaturesYouControl() {
        Permanent jerboa = addCreatureReady(player1, new CanyonJerboa());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        int jerboaPower = jerboa.getEffectivePower();
        int bearPower = bear.getEffectivePower();
        int jerboaToughness = jerboa.getEffectiveToughness();
        int bearToughness = bear.getEffectiveToughness();
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(jerboa.getEffectivePower()).isEqualTo(jerboaPower + 1);
        assertThat(jerboa.getEffectiveToughness()).isEqualTo(jerboaToughness + 1);
        assertThat(bear.getEffectivePower()).isEqualTo(bearPower + 1);
        assertThat(bear.getEffectiveToughness()).isEqualTo(bearToughness + 1);
    }

    @Test
    void opponentsLandDoesNotTriggerLandfall() {
        Permanent jerboa = addCreatureReady(player1, new CanyonJerboa());
        int jerboaPower = jerboa.getEffectivePower();
        int jerboaToughness = jerboa.getEffectiveToughness();
        harness.setHand(player2, List.of(new Forest()));
        harness.forceActivePlayer(player2);

        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(jerboa.getEffectivePower()).isEqualTo(jerboaPower);
        assertThat(jerboa.getEffectiveToughness()).isEqualTo(jerboaToughness);
    }

    @Test
    void landfallBoostExpiresAtEndOfTurn() {
        Permanent jerboa = addCreatureReady(player1, new CanyonJerboa());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        assertThat(jerboa.getPowerModifier()).isEqualTo(1);
        assertThat(jerboa.getToughnessModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(jerboa.getPowerModifier()).isZero();
        assertThat(jerboa.getToughnessModifier()).isZero();
    }
}
