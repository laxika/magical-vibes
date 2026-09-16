package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoblinPyromancer.class, GoblinPiledriver.class, GlorySeeker.class})
class GoblinPyromancerTest extends BaseCardTest {

    @Test
    void enteringBoostsGoblinCreaturesOnAllBattlefields() {
        Permanent ownGoblin = harness.addToBattlefieldAndReturn(player1, new GoblinPiledriver());
        Permanent opponentGoblin = harness.addToBattlefieldAndReturn(player2, new GoblinPiledriver());
        Permanent nonGoblin = harness.addToBattlefieldAndReturn(player1, new GlorySeeker());

        harness.castFromHand(player1, new GoblinPyromancer(), "{3}{R}");
        resolveAllTriggers();

        Permanent pyromancer = findPermanent(player1, "Goblin Pyromancer");
        assertThat(gqs.getEffectivePower(gd, ownGoblin)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, opponentGoblin)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, nonGoblin)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, pyromancer)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, ownGoblin)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentGoblin)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, pyromancer)).isEqualTo(2);
    }

    @Test
    void goblinsEnteringAfterEtbResolutionAreNotBoosted() {
        Permanent existingGoblin = harness.addToBattlefieldAndReturn(player1, new GoblinPiledriver());

        harness.castFromHand(player1, new GoblinPyromancer(), "{3}{R}");
        resolveAllTriggers();

        Permanent laterGoblin = harness.addToBattlefieldAndReturn(player2, new GoblinPiledriver());

        assertThat(gqs.getEffectivePower(gd, existingGoblin)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, laterGoblin)).isEqualTo(1);
    }

    @Test
    void beginningOfEndStepDestroysAllGoblinsButNotOtherCreatures() {
        harness.addToBattlefield(player1, new GoblinPiledriver());
        harness.addToBattlefield(player2, new GoblinPiledriver());
        harness.addToBattlefield(player1, new GlorySeeker());
        harness.addToBattlefield(player1, new GoblinPyromancer());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Goblin Piledriver")).isEmpty();
        assertThat(findPermanents(player2, "Goblin Piledriver")).isEmpty();
        assertThat(findPermanents(player1, "Goblin Pyromancer")).isEmpty();
        assertThat(findPermanents(player1, "Glory Seeker")).hasSize(1);
    }
}
