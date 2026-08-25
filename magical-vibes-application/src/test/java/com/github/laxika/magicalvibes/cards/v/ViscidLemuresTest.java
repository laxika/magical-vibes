package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(ViscidLemures.class)
class ViscidLemuresTest extends BaseCardTest {

    @Test
    @DisplayName("Activating gives -1/-0 and swampwalk until end of turn")
    void debuffsAndGrantsSwampwalk() {
        Permanent lemures = harness.addToBattlefieldAndReturn(player1, new ViscidLemures());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, lemures)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, lemures)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, lemures, Keyword.SWAMPWALK)).isTrue();
    }

    @Test
    @DisplayName("Multiple activations stack the power reduction")
    void multipleActivationsStack() {
        Permanent lemures = harness.addToBattlefieldAndReturn(player1, new ViscidLemures());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, lemures)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, lemures)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, lemures, Keyword.SWAMPWALK)).isTrue();
    }

    @Test
    @DisplayName("The power reduction and swampwalk wear off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent lemures = harness.addToBattlefieldAndReturn(player1, new ViscidLemures());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, lemures)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, lemures)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, lemures, Keyword.SWAMPWALK)).isFalse();
    }
}
