package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SafeholdSentry.class})
class SafeholdSentryTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {2}{W} and untapping gives +0/+2 until end of turn")
    void pumpsToughnessAndUntapsSource() {
        Permanent sentry = addTappedSentry();
        harness.addMana(player1, ManaColor.WHITE, 3);
        enterMainWithPriority(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(sentry.getPowerModifier()).isEqualTo(0);
        assertThat(sentry.getToughnessModifier()).isEqualTo(2);
        // Paying {Q} untapped the source.
        assertThat(sentry.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The +0/+2 boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent sentry = addTappedSentry();
        harness.addMana(player1, ManaColor.WHITE, 3);
        enterMainWithPriority(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(sentry.getToughnessModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(sentry.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot activate while the source is untapped ({Q} requires it to be tapped)")
    void cannotActivateWhileUntapped() {
        addCreatureReady(player1, new SafeholdSentry());
        harness.addMana(player1, ManaColor.WHITE, 3);
        enterMainWithPriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without the full {2}{W} cost")
    void cannotActivateWithoutEnoughMana() {
        Permanent sentry = addTappedSentry();
        harness.addMana(player1, ManaColor.WHITE, 2);
        enterMainWithPriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(sentry.isTapped()).isTrue();
        assertThat(sentry.getToughnessModifier()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate while summoning sick even when tapped")
    void cannotActivateWhileSummoningSick() {
        Permanent sentry = harness.addToBattlefieldAndReturn(player1, new SafeholdSentry());
        sentry.tap();
        harness.addMana(player1, ManaColor.WHITE, 3);
        enterMainWithPriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(sentry.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Untapping is paid immediately, but the boost waits for resolution")
    void untapsAsCostBeforeBoostResolves() {
        Permanent sentry = addTappedSentry();
        harness.addMana(player1, ManaColor.WHITE, 6);
        enterMainWithPriority(player1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(sentry.isTapped()).isFalse();
        assertThat(sentry.getToughnessModifier()).isZero();
        assertThat(gd.stack).hasSize(1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(sentry.getPowerModifier()).isZero();
        assertThat(sentry.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Three colorless mana cannot pay the required white mana")
    void cannotActivateWithoutWhiteMana() {
        Permanent sentry = addTappedSentry();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        enterMainWithPriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(sentry.isTapped()).isTrue();
        assertThat(sentry.getToughnessModifier()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The ability can be activated during an opponent's turn")
    void canActivateDuringOpponentsTurn() {
        Permanent sentry = addTappedSentry();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        enterMainWithPriority(player2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(sentry.isTapped()).isFalse();
        assertThat(sentry.getPowerModifier()).isZero();
        assertThat(sentry.getToughnessModifier()).isEqualTo(2);
    }

    private Permanent addTappedSentry() {
        Permanent sentry = addCreatureReady(player1, new SafeholdSentry());
        sentry.tap();
        return sentry;
    }

    private void enterMainWithPriority(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
