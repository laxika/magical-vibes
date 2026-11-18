package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
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
        int jerboaPower = gqs.getEffectivePower(gd, jerboa);
        int bearPower = gqs.getEffectivePower(gd, bear);
        int jerboaToughness = gqs.getEffectiveToughness(gd, jerboa);
        int bearToughness = gqs.getEffectiveToughness(gd, bear);
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, jerboa)).isEqualTo(jerboaPower + 1);
        assertThat(gqs.getEffectiveToughness(gd, jerboa)).isEqualTo(jerboaToughness + 1);
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(bearPower + 1);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(bearToughness + 1);
    }

    @Test
    void opponentsLandDoesNotTriggerLandfall() {
        Permanent jerboa = addCreatureReady(player1, new CanyonJerboa());
        int jerboaPower = gqs.getEffectivePower(gd, jerboa);
        int jerboaToughness = gqs.getEffectiveToughness(gd, jerboa);
        harness.setHand(player2, List.of(new Forest()));
        harness.forceActivePlayer(player2);

        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, jerboa)).isEqualTo(jerboaPower);
        assertThat(gqs.getEffectiveToughness(gd, jerboa)).isEqualTo(jerboaToughness);
    }

    @Test
    void landfallBoostExpiresAtEndOfTurn() {
        Permanent jerboa = addCreatureReady(player1, new CanyonJerboa());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        assertThat(PermanentGrants.powerBoostOf(gd, jerboa)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, jerboa)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, jerboa)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, jerboa)).isZero();
    }

    @Test
    void landsEnteringWithoutBeingPlayedEachTriggerLandfall() {
        Permanent jerboa = addCreatureReady(player1, new CanyonJerboa());
        Permanent opponent = addCreatureReady(player2, new CanyonJerboa());

        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.enterBattlefieldAndReturn(player1, new Forest());

        assertThat(gd.stack).hasSize(2);
        assertThat(PermanentGrants.powerBoostOf(gd, jerboa)).isZero();
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, jerboa)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, jerboa)).isEqualTo(2);
        assertThat(PermanentGrants.powerBoostOf(gd, opponent)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, opponent)).isZero();
    }

    @Test
    void creaturesAreDeterminedWhenLandfallResolves() {
        Permanent source = addCreatureReady(player1, new CanyonJerboa());
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        Permanent beforeResolution = harness.enterBattlefieldAndReturn(player1, new CanyonJerboa());

        resolveAllTriggers();
        Permanent afterResolution = harness.enterBattlefieldAndReturn(player1, new CanyonJerboa());

        assertThat(PermanentGrants.powerBoostOf(gd, source)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, source)).isEqualTo(1);
        assertThat(PermanentGrants.powerBoostOf(gd, beforeResolution)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, beforeResolution)).isEqualTo(1);
        assertThat(PermanentGrants.powerBoostOf(gd, afterResolution)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, afterResolution)).isZero();
    }

    @Test
    void landfallStillResolvesAfterItsSourceDies() {
        Permanent source = addCreatureReady(player1, new CanyonJerboa());
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        Permanent survivor = harness.enterBattlefieldAndReturn(player1, new CanyonJerboa());

        PermanentGrants.setToughnessBoost(gd, source, -2);
        harness.runStateBasedActions();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(source);
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, survivor)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, survivor)).isEqualTo(1);
    }
}
