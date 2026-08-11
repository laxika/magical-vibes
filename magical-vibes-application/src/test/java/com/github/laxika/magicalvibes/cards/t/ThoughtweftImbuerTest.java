package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KnightOfMeadowgrain;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ThoughtweftImbuerTest extends BaseCardTest {

    @Test
    @DisplayName("A creature attacking alone gets +X/+X for each Kithkin you control")
    void boostsAloneAttackerByKithkinCount() {
        addCreatureReady(player1, new ThoughtweftImbuer());
        addCreatureReady(player1, new KnightOfMeadowgrain());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(2));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    @DisplayName("The boost does not count an opponent's Kithkin")
    void countsOnlyYourKithkin() {
        addCreatureReady(player1, new ThoughtweftImbuer());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new KnightOfMeadowgrain());

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("The ability does not trigger when more than one creature attacks")
    void doesNotTriggerWhenAttackingWithMoreThanOneCreature() {
        addCreatureReady(player1, new ThoughtweftImbuer());
        Permanent kithkin = addCreatureReady(player1, new KnightOfMeadowgrain());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1, 2));

        assertThat(gqs.getEffectivePower(gd, kithkin)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, kithkin)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new ThoughtweftImbuer());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }
}
