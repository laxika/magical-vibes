package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HoppingAutomatonTest extends BaseCardTest {

    @Test
    @DisplayName("Activating gives -1/-1 and flying until end of turn")
    void boostsAndGrantsFlying() {
        Permanent automaton = harness.addToBattlefieldAndReturn(player1, new HoppingAutomaton());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, automaton)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, automaton)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, automaton, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("The reduction and flying wear off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent automaton = harness.addToBattlefieldAndReturn(player1, new HoppingAutomaton());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, automaton)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, automaton)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, automaton, Keyword.FLYING)).isFalse();
    }
}
