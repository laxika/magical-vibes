package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TuyaBearclaw.class, CrawWurm.class, GrizzlyBears.class})
class TuyaBearclawTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +X/+X based on the greatest power among other creatures you control")
    void boostsByGreatestOtherControlledPower() {
        var tuya = addCreatureReady(player1, new TuyaBearclaw());
        addCreatureReady(player1, new CrawWurm());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, tuya)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, tuya)).isEqualTo(8);
    }

    @Test
    @DisplayName("Does not count itself or creatures controlled by an opponent")
    void ignoresSelfAndOpponentsCreatures() {
        var tuya = addCreatureReady(player1, new TuyaBearclaw());
        addCreatureReady(player2, new CrawWurm());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, tuya)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, tuya)).isEqualTo(2);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        var tuya = addCreatureReady(player1, new TuyaBearclaw());
        addCreatureReady(player1, new CrawWurm());

        declareAttackers(List.of(0));
        resolveAllTriggers();
        assertThat(tuya.getPowerModifier()).isEqualTo(6);
        assertThat(tuya.getToughnessModifier()).isEqualTo(6);

        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(tuya.getPowerModifier()).isZero();
        assertThat(tuya.getToughnessModifier()).isZero();
    }
}
