package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SentryOfTheUnderworldTest extends BaseCardTest {

    @Test
    @DisplayName("Paying white, black, and 3 life grants a regeneration shield")
    void payingAbilityCostGrantsRegenerationShield() {
        Permanent sentry = addSentryReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(sentry.getRegenerationShield()).isEqualTo(1);
        assertThat(harness.getGameData().playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 3);
    }

    @Test
    @DisplayName("Activating regeneration does not tap Sentry of the Underworld")
    void activationDoesNotTapSentry() {
        Permanent sentry = addSentryReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(sentry.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate regeneration without both colored mana")
    void cannotActivateWithoutBothColors() {
        addSentryReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Cannot activate regeneration without 3 life to pay")
    void cannotActivateWithoutEnoughLife() {
        addSentryReady(player1);
        harness.setLife(player1, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addSentryReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new SentryOfTheUnderworld());
        perm.setSummoningSick(false);
        return perm;
    }
}
