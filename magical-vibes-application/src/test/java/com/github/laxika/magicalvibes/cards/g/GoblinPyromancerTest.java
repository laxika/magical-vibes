package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoblinPyromancer.class, GoblinPiker.class, GrizzlyBears.class})
class GoblinPyromancerTest extends BaseCardTest {

    @Test
    void enteringBoostsGoblinCreaturesOnAllBattlefields() {
        Permanent ownGoblin = harness.addToBattlefieldAndReturn(player1, new GoblinPiker());
        Permanent opponentGoblin = harness.addToBattlefieldAndReturn(player2, new GoblinPiker());
        Permanent nonGoblin = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new GoblinPyromancer()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownGoblin)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, opponentGoblin)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, nonGoblin)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownGoblin)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentGoblin)).isEqualTo(1);
    }

    @Test
    void beginningOfEndStepDestroysAllGoblinsButNotOtherCreatures() {
        harness.addToBattlefield(player1, new GoblinPiker());
        harness.addToBattlefield(player2, new GoblinPiker());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GoblinPyromancer());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Goblin Piker")).isEmpty();
        assertThat(findPermanents(player2, "Goblin Piker")).isEmpty();
        assertThat(findPermanents(player1, "Goblin Pyromancer")).isEmpty();
        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(1);
    }
}
