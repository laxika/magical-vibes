package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ForgottenAncient.class, GrizzlyBears.class, Spellbook.class})
class ForgottenAncientTest extends BaseCardTest {

    @Test
    void addsACounterWhenAnyPlayerCastsASpellAndTheTriggerIsAccepted() {
        Permanent ancient = addCreatureReady(player1, new ForgottenAncient());
        harness.setHand(player2, List.of(new Spellbook()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castArtifact(player2, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(ancient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void movesChosenCountersAmongOtherCreatures() {
        Permanent ancient = addCreatureReady(player1, new ForgottenAncient());
        Permanent ownBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingBear = addCreatureReady(player2, new GrizzlyBears());
        ancient.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        harness.forceActivePlayer(player1);
        gd.turnNumber = 2;
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleXValueChosen(player1, 3);
        harness.handleXValueChosen(player1, 1);
        harness.handleXValueChosen(player1, 2);

        assertThat(ancient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(ownBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opposingBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void choosingZeroLeavesCountersOnTheAncient() {
        Permanent ancient = addCreatureReady(player1, new ForgottenAncient());
        addCreatureReady(player1, new GrizzlyBears());
        ancient.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.forceActivePlayer(player1);
        gd.turnNumber = 2;
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleXValueChosen(player1, 0);

        assertThat(ancient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }
}
