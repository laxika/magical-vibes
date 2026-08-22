package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.a.ArmageddonClock;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ObekaSplitterOfSeconds.class, ArmageddonClock.class, SuntailHawk.class})
class ObekaSplitterOfSecondsTest extends BaseCardTest {

    @Test
    @DisplayName("Gets additional upkeep steps equal to combat damage dealt to a player")
    void getsAdditionalUpkeepStepsEqualToCombatDamage() {
        Permanent obeka = addCreatureReady(player1, new ObekaSplitterOfSeconds());
        Permanent clock = harness.addToBattlefieldAndReturn(player1, new ArmageddonClock());
        obeka.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);

        assertThat(clock.getCounterCount(CounterType.DOOM)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not get additional upkeep steps when no combat damage reaches a player")
    void noAdditionalUpkeepStepsWithoutCombatDamageToPlayer() {
        Permanent obeka = addCreatureReady(player1, new ObekaSplitterOfSeconds());
        Permanent clock = harness.addToBattlefieldAndReturn(player1, new ArmageddonClock());
        Permanent firstBlocker = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());
        Permanent secondBlocker = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());
        obeka.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        harness.passBothPriorities();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                firstBlocker.getId(), 1,
                secondBlocker.getId(), 1));
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);

        assertThat(clock.getCounterCount(CounterType.DOOM)).isZero();
    }
}
