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

    @Test
    void landsEnteringWithoutBeingPlayedEachTriggerLandfall() {
        Permanent jerboa = addCreatureReady(player1, new CanyonJerboa());
        Permanent opponent = addCreatureReady(player2, new CanyonJerboa());

        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.enterBattlefieldAndReturn(player1, new Forest());

        assertThat(gd.stack).hasSize(2);
        assertThat(jerboa.getPowerModifier()).isZero();
        resolveAllTriggers();

        assertThat(jerboa.getPowerModifier()).isEqualTo(2);
        assertThat(jerboa.getToughnessModifier()).isEqualTo(2);
        assertThat(opponent.getPowerModifier()).isZero();
        assertThat(opponent.getToughnessModifier()).isZero();
    }

    @Test
    void creaturesAreDeterminedWhenLandfallResolves() {
        Permanent source = addCreatureReady(player1, new CanyonJerboa());
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        Permanent beforeResolution = harness.enterBattlefieldAndReturn(player1, new CanyonJerboa());

        resolveAllTriggers();
        Permanent afterResolution = harness.enterBattlefieldAndReturn(player1, new CanyonJerboa());

        assertThat(source.getPowerModifier()).isEqualTo(1);
        assertThat(source.getToughnessModifier()).isEqualTo(1);
        assertThat(beforeResolution.getPowerModifier()).isEqualTo(1);
        assertThat(beforeResolution.getToughnessModifier()).isEqualTo(1);
        assertThat(afterResolution.getPowerModifier()).isZero();
        assertThat(afterResolution.getToughnessModifier()).isZero();
    }

    @Test
    void landfallStillResolvesAfterItsSourceDies() {
        Permanent source = addCreatureReady(player1, new CanyonJerboa());
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        Permanent survivor = harness.enterBattlefieldAndReturn(player1, new CanyonJerboa());

        source.setToughnessModifier(-2);
        harness.runStateBasedActions();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(source);
        resolveAllTriggers();

        assertThat(survivor.getPowerModifier()).isEqualTo(1);
        assertThat(survivor.getToughnessModifier()).isEqualTo(1);
    }
}
