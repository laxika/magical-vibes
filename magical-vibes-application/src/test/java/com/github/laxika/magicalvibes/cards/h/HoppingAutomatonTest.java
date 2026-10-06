package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HoppingAutomaton.class})
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
    @DisplayName("Activating does not tap the automaton")
    void activationDoesNotTapAutomaton() {
        Permanent automaton = harness.addToBattlefieldAndReturn(player1, new HoppingAutomaton());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(automaton.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The reduction and flying wear off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent automaton = harness.addToBattlefieldAndReturn(player1, new HoppingAutomaton());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, automaton)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, automaton)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, automaton, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("A second activation reduces toughness to zero and puts the automaton in the graveyard")
    void repeatedActivationsReduceToughnessToZero() {
        harness.addToBattlefield(player1, new HoppingAutomaton());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Hopping Automaton");

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Hopping Automaton");
        harness.assertInGraveyard(player1, "Hopping Automaton");
    }

    @Test
    @DisplayName("The ability affects only its source, not another automaton controlled by either player")
    void affectsOnlyItsSource() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HoppingAutomaton());
        Permanent friendly = harness.addToBattlefieldAndReturn(player1, new HoppingAutomaton());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new HoppingAutomaton());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, source)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, source, Keyword.FLYING)).isTrue();
        for (Permanent other : new Permanent[]{friendly, opposing}) {
            assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(2);
            assertThat(gqs.hasKeyword(gd, other, Keyword.FLYING)).isFalse();
        }
    }

    @Test
    @DisplayName("A tapped, summoning-sick automaton can activate and changes only when the ability resolves")
    void tappedSummoningSickSourceCanActivate() {
        Permanent automaton = harness.addToBattlefieldAndReturn(player1, new HoppingAutomaton());
        automaton.tap();
        automaton.setSummoningSick(true);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, automaton)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, automaton)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, automaton, Keyword.FLYING)).isFalse();

        harness.passBothPriorities();

        assertThat(automaton.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, automaton)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, automaton)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, automaton, Keyword.FLYING)).isTrue();
    }
}
