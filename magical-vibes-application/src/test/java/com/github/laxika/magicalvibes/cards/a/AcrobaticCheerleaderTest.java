package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(AcrobaticCheerleader.class)
class AcrobaticCheerleaderTest extends BaseCardTest {

    @Test
    void tappedCreatureGetsFlyingCounterAtPostcombatMain() {
        Permanent cheerleader = harness.addToBattlefieldAndReturn(player1, new AcrobaticCheerleader());
        cheerleader.tap();

        advanceToPostcombatMain(player1);

        assertThat(gd.stack).singleElement().satisfies(entry ->
                assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY));

        harness.passBothPriorities();

        assertThat(cheerleader.getCounterCount(CounterType.FLYING)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, cheerleader, Keyword.FLYING)).isTrue();
    }

    @Test
    void untappedCreatureDoesNotTriggerLaterInTheTurn() {
        Permanent cheerleader = harness.addToBattlefieldAndReturn(player1, new AcrobaticCheerleader());

        advanceToPostcombatMain(player1);

        cheerleader.tap();
        advanceToPostcombatMain(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(cheerleader.getCounterCount(CounterType.FLYING)).isZero();
    }

    @Test
    void untappingBeforeResolutionPreventsCounter() {
        Permanent cheerleader = harness.addToBattlefieldAndReturn(player1, new AcrobaticCheerleader());
        cheerleader.tap();

        advanceToPostcombatMain(player1);
        cheerleader.untap();
        harness.passBothPriorities();

        assertThat(cheerleader.getCounterCount(CounterType.FLYING)).isZero();
    }

    @Test
    void abilityTriggersOnlyOnceForThePermanentObject() {
        Permanent cheerleader = harness.addToBattlefieldAndReturn(player1, new AcrobaticCheerleader());
        cheerleader.tap();

        advanceToPostcombatMain(player1);
        harness.passBothPriorities();

        cheerleader.untap();
        cheerleader.tap();
        advanceToPostcombatMain(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(cheerleader.getCounterCount(CounterType.FLYING)).isEqualTo(1);
    }

    private void advanceToPostcombatMain(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
    }
}
