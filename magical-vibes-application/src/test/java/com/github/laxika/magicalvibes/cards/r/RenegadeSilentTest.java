package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RenegadeSilent.class, GrizzlyBears.class, FountainOfYouth.class})
class RenegadeSilentTest extends BaseCardTest {

    @Test
    @DisplayName("Goads an opposing creature, gets a counter, and phases out at end step")
    void goadsOpposingCreatureGetsCounterAndPhasesOut() {
        Permanent silent = harness.addToBattlefieldAndReturn(player1, new RenegadeSilent());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent noncreature = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToEndStep();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds())
                .containsExactly(opposingCreature.getId());
        harness.handlePermanentChosen(player1, opposingCreature.getId());
        harness.passBothPriorities();

        assertThat(silent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(silent);
        assertThat(als.getMustAttackRequirementCount(gd, opposingCreature)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(noncreature, opposingCreature);
    }

    @Test
    @DisplayName("Still gets a counter and phases out when no opposing creature is available")
    void resolvesWithoutTarget() {
        Permanent silent = harness.addToBattlefieldAndReturn(player1, new RenegadeSilent());

        advanceToEndStep();
        harness.passBothPriorities();

        assertThat(silent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(silent);
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
