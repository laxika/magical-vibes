package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(MerrowWavebreakers.class)
class MerrowWavebreakersTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {1}{U} and untapping grants flying until end of turn")
    void grantsFlyingAndUntaps() {
        Permanent wavebreakers = addCreatureReady(player1, new MerrowWavebreakers());
        wavebreakers.tap();
        harness.addMana(player1, ManaColor.BLUE, 2);

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

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(wavebreakers.hasKeyword(Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(wavebreakers.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Cannot activate while the source is untapped ({Q} requires it to be tapped)")
    void cannotActivateWhileUntapped() {
        addCreatureReady(player1, new MerrowWavebreakers());
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not tapped");
    }

    @Test
    @DisplayName("Cannot activate without the full {1}{U} cost")
    void cannotActivateWithoutEnoughMana() {
        Permanent wavebreakers = addCreatureReady(player1, new MerrowWavebreakers());
        wavebreakers.tap();
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(wavebreakers.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot pay {Q} with a summoning-sick creature")
    void cannotActivateWhileSummoningSick() {
        Permanent wavebreakers = harness.addToBattlefieldAndReturn(player1, new MerrowWavebreakers());
        wavebreakers.tap();
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        assertThat(wavebreakers.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Untapping is paid immediately, but flying waits for resolution")
    void untapsBeforeFlyingIsGranted() {
        Permanent wavebreakers = addCreatureReady(player1, new MerrowWavebreakers());
        wavebreakers.tap();
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(wavebreakers.isTapped()).isFalse();
        assertThat(wavebreakers.hasKeyword(Keyword.FLYING)).isFalse();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(wavebreakers.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Only the activating source gains flying")
    void grantsFlyingOnlyToSource() {
        Permanent wavebreakers = addCreatureReady(player1, new MerrowWavebreakers());
        Permanent ally = addCreatureReady(player1, new MerrowWavebreakers());
        Permanent opponent = addCreatureReady(player2, new MerrowWavebreakers());
        wavebreakers.tap();
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(wavebreakers.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(ally.hasKeyword(Keyword.FLYING)).isFalse();
        assertThat(opponent.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("The generic mana cost can be paid with another color")
    void acceptsNonblueManaForGenericCost() {
        Permanent wavebreakers = addCreatureReady(player1, new MerrowWavebreakers());
        wavebreakers.tap();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(wavebreakers.isTapped()).isFalse();
        assertThat(wavebreakers.hasKeyword(Keyword.FLYING)).isTrue();
    }
}
