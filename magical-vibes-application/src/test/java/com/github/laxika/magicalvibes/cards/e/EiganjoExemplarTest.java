package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MothriderSamurai;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EiganjoExemplar.class, ElvishWarrior.class, GrizzlyBears.class, MothriderSamurai.class})
class EiganjoExemplarTest extends BaseCardTest {

    @Test
    @DisplayName("A Samurai attacking alone gets +1/+1 until end of turn")
    void samuraiAttackingAloneGetsBoosted() {
        addCreatureReady(player1, new EiganjoExemplar());
        Permanent samurai = addCreatureReady(player1, new MothriderSamurai());

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, samurai)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, samurai)).isEqualTo(2);
    }

    @Test
    @DisplayName("A Warrior attacking alone gets +1/+1 until end of turn")
    void warriorAttackingAloneGetsBoosted() {
        addCreatureReady(player1, new EiganjoExemplar());
        Permanent warrior = addCreatureReady(player1, new ElvishWarrior());

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, warrior)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, warrior)).isEqualTo(3);
    }

    @Test
    @DisplayName("A non-Samurai, non-Warrior attacking alone is not boosted")
    void otherCreatureAttackingAloneIsNotBoosted() {
        addCreatureReady(player1, new EiganjoExemplar());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("The trigger does not fire when multiple creatures attack")
    void multipleAttackersAreNotBoosted() {
        addCreatureReady(player1, new EiganjoExemplar());
        Permanent warrior = addCreatureReady(player1, new ElvishWarrior());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1, 2));

        assertThat(gqs.getEffectivePower(gd, warrior)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, warrior)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new EiganjoExemplar());
        Permanent warrior = addCreatureReady(player1, new ElvishWarrior());

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, warrior)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, warrior)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, warrior)).isEqualTo(2);
    }
}
