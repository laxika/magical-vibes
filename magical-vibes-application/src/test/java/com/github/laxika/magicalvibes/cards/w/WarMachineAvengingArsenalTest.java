package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WarMachineAvengingArsenal.class, GrizzlyBears.class})
class WarMachineAvengingArsenalTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking gives double strike to attacking modified creatures you control")
    void attackingGivesDoubleStrikeToAttackingModifiedCreatures() {
        Permanent warMachine = addCreatureReady(player1, new WarMachineAvengingArsenal());
        Permanent modifiedAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent unmodifiedAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent nonAttacker = addCreatureReady(player1, new GrizzlyBears());
        warMachine.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        modifiedAttacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackers(player1, List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, warMachine, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, modifiedAttacker, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, unmodifiedAttacker, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, nonAttacker, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("The attack trigger's double strike grant wears off at end of turn")
    void doubleStrikeGrantWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new WarMachineAvengingArsenal());
        Permanent modifiedAttacker = addCreatureReady(player1, new GrizzlyBears());
        modifiedAttacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();
        assertThat(gqs.hasKeyword(gd, modifiedAttacker, Keyword.DOUBLE_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, modifiedAttacker, Keyword.DOUBLE_STRIKE)).isFalse();
    }
}
