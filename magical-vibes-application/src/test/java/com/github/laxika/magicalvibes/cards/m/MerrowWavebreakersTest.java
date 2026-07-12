package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MerrowWavebreakersTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {1}{U} and untapping grants flying until end of turn")
    void grantsFlyingAndUntaps() {
        Permanent wavebreakers = addCreatureReady(player1, new MerrowWavebreakers());
        wavebreakers.tap();
        harness.addMana(player1, ManaColor.BLUE, 2);

        enterMainWithPriority();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(wavebreakers.hasKeyword(Keyword.FLYING)).isTrue();
        // Paying {Q} untapped the source.
        assertThat(wavebreakers.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The granted flying wears off at end of turn")
    void flyingWearsOff() {
        Permanent wavebreakers = addCreatureReady(player1, new MerrowWavebreakers());
        wavebreakers.tap();
        harness.addMana(player1, ManaColor.BLUE, 2);

        enterMainWithPriority();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(wavebreakers.hasKeyword(Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(wavebreakers.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Cannot activate while the source is untapped ({Q} requires it to be tapped)")
    void cannotActivateWhileUntapped() {
        addCreatureReady(player1, new MerrowWavebreakers());
        harness.addMana(player1, ManaColor.BLUE, 2);

        enterMainWithPriority();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not tapped");
    }

    private void enterMainWithPriority() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
