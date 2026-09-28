package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FrayingLine.class, GrizzlyBears.class})
class FrayingLineTest extends BaseCardTest {

    @Test
    void entersWithRopeCounterOnTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, java.util.List.of(new FrayingLine()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.ROPE)).isEqualTo(1);
    }

    @Test
    void activePlayerPaysToRopeOneOfTheirCreatures() {
        Permanent line = harness.addToBattlefieldAndReturn(player1, new FrayingLine());
        Permanent sourceControllerCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent activeCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        advanceToUpkeepWithRopeTrigger(player2);
        harness.passBothPriorities();
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(line, sourceControllerCreature);
        Permanent currentActiveCreature = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getId().equals(activeCreature.getId()))
                .findFirst().orElseThrow();
        assertThat(currentActiveCreature.getCounterCount(CounterType.ROPE)).isEqualTo(1);
        assertThat(sourceControllerCreature.getCounterCount(CounterType.ROPE)).isZero();
    }

    @Test
    void decliningExilesUnropedCreaturesAndClearsRopeCounters() {
        Permanent line = harness.addToBattlefieldAndReturn(player1, new FrayingLine());
        Permanent unroped = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent roped = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        roped.setCounterCount(CounterType.ROPE, 2);

        advanceToUpkeepWithRopeTrigger(player2);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(line, unroped);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(roped);
        assertThat(roped.getCounterCount(CounterType.ROPE)).isZero();
    }

    private void advanceToUpkeepWithRopeTrigger(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
    }
}
