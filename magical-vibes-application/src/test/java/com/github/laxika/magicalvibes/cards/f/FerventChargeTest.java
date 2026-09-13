package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GaeasSkyfolk;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FerventCharge.class, GaeasSkyfolk.class})
class FerventChargeTest extends BaseCardTest {

    @Test
    void boostsEachCreatureYouControlThatAttacks() {
        harness.addToBattlefield(player1, new FerventCharge());
        Permanent firstSkyfolk = addCreatureReady(player1, new GaeasSkyfolk());
        Permanent secondSkyfolk = addCreatureReady(player1, new GaeasSkyfolk());

        declareAttackers(List.of(1, 2));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, firstSkyfolk)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, firstSkyfolk)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, secondSkyfolk)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, secondSkyfolk)).isEqualTo(4);
    }

    @Test
    void doesNotBoostYourCreaturesThatDoNotAttack() {
        harness.addToBattlefield(player1, new FerventCharge());
        Permanent attacker = addCreatureReady(player1, new GaeasSkyfolk());
        Permanent nonAttacker = addCreatureReady(player1, new GaeasSkyfolk());

        declareAttackers(List.of(1));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, nonAttacker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, nonAttacker)).isEqualTo(2);
    }

    @Test
    void doesNotTriggerForOpponentCreaturesAttacking() {
        harness.addToBattlefield(player1, new FerventCharge());
        Permanent skyfolk = addCreatureReady(player2, new GaeasSkyfolk());

        declareAttackers(player2, List.of(0));

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, skyfolk)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, skyfolk)).isEqualTo(2);
    }

    @Test
    void boostWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new FerventCharge());
        Permanent skyfolk = addCreatureReady(player1, new GaeasSkyfolk());

        declareAttackers(List.of(1));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, skyfolk)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, skyfolk)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, skyfolk)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, skyfolk)).isEqualTo(2);
    }
}
