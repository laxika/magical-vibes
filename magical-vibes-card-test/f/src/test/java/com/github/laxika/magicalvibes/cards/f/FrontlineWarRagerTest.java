package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.i.IntrepidTenderfoot;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FrontlineWarRager.class, IntrepidTenderfoot.class, Island.class})
class FrontlineWarRagerTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on itself with two tapped creatures")
    void putsCounterWithTwoTappedCreatures() {
        Permanent rager = harness.addToBattlefieldAndReturn(player1, new FrontlineWarRager());
        addTappedCreature(player1);
        addTappedCreature(player1);

        advanceToEndStep(player1);

        assertThat(rager.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not put a counter on itself with fewer than two tapped creatures")
    void doesNotPutCounterWithFewerThanTwoTappedCreatures() {
        Permanent rager = harness.addToBattlefieldAndReturn(player1, new FrontlineWarRager());
        addTappedCreature(player1);
        harness.addToBattlefield(player1, new IntrepidTenderfoot());

        advanceToEndStep(player1);

        assertThat(rager.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Counts only tapped creatures controlled by its controller")
    void countsOnlyControlledTappedCreatures() {
        Permanent rager = harness.addToBattlefieldAndReturn(player1, new FrontlineWarRager());
        addTappedCreature(player2);
        addTappedCreature(player2);

        advanceToEndStep(player1);

        assertThat(rager.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void countsItselfWhenTapped() {
        Permanent rager = harness.addToBattlefieldAndReturn(player1, new FrontlineWarRager());
        rager.tap();
        addTappedCreature(player1);

        advanceToEndStep(player1);

        assertThat(rager.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotCountTappedNoncreatures() {
        Permanent rager = harness.addToBattlefieldAndReturn(player1, new FrontlineWarRager());
        addTappedCreature(player1);
        harness.addToBattlefieldAndReturn(player1, new Island()).tap();

        advanceToEndStep(player1);

        assertThat(rager.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotTriggerDuringOpponentsEndStep() {
        Permanent rager = harness.addToBattlefieldAndReturn(player1, new FrontlineWarRager());
        addTappedCreature(player1);
        addTappedCreature(player1);

        advanceToEndStep(player2);

        assertThat(rager.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void rechecksTappedCreatureCountAtResolution() {
        Permanent rager = harness.addToBattlefieldAndReturn(player1, new FrontlineWarRager());
        Permanent creature = addTappedCreature(player1);
        addTappedCreature(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);

        creature.untap();
        harness.withAutoStop(TurnStep.END_STEP, harness::passBothPriorities);

        assertThat(gd.stack).isEmpty();
        assertThat(rager.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void doesNotTriggerWhenSecondCreatureBecomesTappedAfterEndStepBegins() {
        Permanent rager = harness.addToBattlefieldAndReturn(player1, new FrontlineWarRager());
        addTappedCreature(player1);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new IntrepidTenderfoot());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.stack).isEmpty();

        creature.tap();
        harness.withAutoStop(TurnStep.END_STEP, harness::passBothPriorities);

        assertThat(rager.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void putsOnlyOneCounterWithMoreThanTwoTappedCreatures() {
        Permanent rager = harness.addToBattlefieldAndReturn(player1, new FrontlineWarRager());
        addTappedCreature(player1);
        addTappedCreature(player1);
        addTappedCreature(player1);

        advanceToEndStep(player1);

        assertThat(rager.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private Permanent addTappedCreature(Player player) {
        Permanent creature = harness.addToBattlefieldAndReturn(player, new IntrepidTenderfoot());
        creature.tap();
        return creature;
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.END_STEP);
        if (!gd.stack.isEmpty()) {
            harness.withAutoStop(TurnStep.END_STEP, harness::passBothPriorities);
        }
    }
}
