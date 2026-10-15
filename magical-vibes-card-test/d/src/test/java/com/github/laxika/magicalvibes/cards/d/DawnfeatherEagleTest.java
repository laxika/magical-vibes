package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AegisAutomaton;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DawnfeatherEagle.class, AegisAutomaton.class})
class DawnfeatherEagleTest extends BaseCardTest {

    private void castEagle() {
        harness.castFromHand(player1, new DawnfeatherEagle(), "{4}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Entering boosts and grants vigilance to creatures you control, including itself")
    void boostsAndGrantsVigilance() {
        harness.addToBattlefield(player1, new AegisAutomaton());

        castEagle();

        Permanent automaton = findPermanent(player1, "Aegis Automaton");
        assertThat(automaton.getEffectivePower()).isEqualTo(1);
        assertThat(automaton.getEffectiveToughness()).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, automaton, Keyword.VIGILANCE)).isTrue();

        Permanent eagle = findPermanent(player1, "Dawnfeather Eagle");
        assertThat(eagle.getEffectivePower()).isEqualTo(4);
        assertThat(eagle.getEffectiveToughness()).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, eagle, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Does not affect opponent's creatures")
    void doesNotAffectOpponentCreatures() {
        harness.addToBattlefield(player2, new AegisAutomaton());

        castEagle();

        Permanent automaton = findPermanent(player2, "Aegis Automaton");
        assertThat(automaton.getEffectivePower()).isEqualTo(0);
        assertThat(gqs.hasKeyword(gd, automaton, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Boost and vigilance wear off at end of turn")
    void wearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new AegisAutomaton());

        castEagle();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        Permanent automaton = findPermanent(player1, "Aegis Automaton");
        assertThat(automaton.getEffectivePower()).isEqualTo(0);
        assertThat(gqs.hasKeyword(gd, automaton, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Creatures entering after the ability resolves do not receive its bonuses")
    void doesNotAffectLaterCreatures() {
        castEagle();

        Permanent automaton = harness.addToBattlefieldAndReturn(player1, new AegisAutomaton());

        assertThat(automaton.getEffectivePower()).isEqualTo(0);
        assertThat(automaton.getEffectiveToughness()).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, automaton, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("The ability affects creatures present at resolution, including ones entering after it triggers")
    void affectsCreaturesPresentAtResolution() {
        harness.castFromHand(player1, new DawnfeatherEagle(), "{4}{W}");
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        Permanent automaton = harness.addToBattlefieldAndReturn(player1, new AegisAutomaton());
        harness.passBothPriorities();

        assertThat(automaton.getEffectivePower()).isEqualTo(1);
        assertThat(automaton.getEffectiveToughness()).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, automaton, Keyword.VIGILANCE)).isTrue();
    }
}
