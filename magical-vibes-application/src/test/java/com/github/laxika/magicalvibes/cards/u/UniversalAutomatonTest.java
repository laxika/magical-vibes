package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.k.KingOfThePride;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UniversalAutomaton.class, KingOfThePride.class})
class UniversalAutomatonTest extends BaseCardTest {

    @Test
    @DisplayName("Changeling makes Universal Automaton a Cat")
    void changelingMakesUniversalAutomatonACat() {
        harness.addToBattlefield(player1, new KingOfThePride());
        Permanent automaton = harness.addToBattlefieldAndReturn(player1, new UniversalAutomaton());

        assertThat(gqs.getEffectivePower(gd, automaton)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, automaton)).isEqualTo(2);
    }
}
