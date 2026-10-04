package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RatColony;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OgreChitterlord.class, RatColony.class, GrizzlyBears.class})
class OgreChitterlordTest extends BaseCardTest {

    @Test
    @DisplayName("Entering creates two Rat tokens that can't block")
    void enteringCreatesNonblockingRats() {
        harness.enterBattlefieldAndReturn(player1, new OgreChitterlord());
        harness.passBothPriorities();

        List<Permanent> rats = findPermanents(player1, "Rat");
        assertThat(rats).hasSize(2);
        assertThat(rats).allMatch(rat -> !bls.canBlock(gd, rat));
    }

    @Test
    @DisplayName("The Rat threshold boost applies after the entering trigger creates tokens")
    void enteringBoostsRatsWhenThresholdIsReached() {
        Permanent existingRatOne = addCreatureReady(player1, new RatColony());
        addCreatureReady(player1, new RatColony());
        addCreatureReady(player1, new RatColony());

        harness.enterBattlefieldAndReturn(player1, new OgreChitterlord());
        harness.passBothPriorities();

        assertThat(existingRatOne.getPowerModifier()).isEqualTo(2);
        assertThat(findPermanents(player1, "Rat"))
                .hasSize(2)
                .allMatch(rat -> rat.getPowerModifier() == 2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(existingRatOne.getPowerModifier()).isZero();
        assertThat(findPermanents(player1, "Rat"))
                .allMatch(rat -> rat.getPowerModifier() == 0);
    }

    @Test
    @DisplayName("The attack trigger creates and boosts Rats")
    void attackingCreatesAndBoostsRats() {
        Permanent ogre = addCreatureReady(player1, new OgreChitterlord());
        addCreatureReady(player1, new RatColony());
        addCreatureReady(player1, new RatColony());
        addCreatureReady(player1, new RatColony());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(ogre)));
        gs.declareBlockers(gd, player2, List.of());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Rat"))
                .hasSize(2)
                .allMatch(rat -> rat.getPowerModifier() == 2);
    }

    @Test
    @DisplayName("The threshold boost does not apply below five Rats")
    void enteringDoesNotBoostBelowThreshold() {
        addCreatureReady(player1, new RatColony());
        addCreatureReady(player1, new RatColony());

        harness.enterBattlefieldAndReturn(player1, new OgreChitterlord());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Rat"))
                .hasSize(2)
                .allMatch(rat -> rat.getPowerModifier() == 0);
    }
}
