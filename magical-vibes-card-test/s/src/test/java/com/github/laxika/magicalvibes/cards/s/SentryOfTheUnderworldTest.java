package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.h.HerosDownfall;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SentryOfTheUnderworld.class, HerosDownfall.class})
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

    @Test
    @DisplayName("Life is paid immediately but the regeneration shield waits for resolution")
    void lifeIsPaidBeforeResolution() {
        Permanent sentry = addSentryReady(player1);
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);

        harness.assertLife(player1, 7);
        assertThat(sentry.getRegenerationShield()).isZero();

        harness.passBothPriorities();

        assertThat(sentry.getRegenerationShield()).isEqualTo(1);
        harness.assertLife(player1, 7);
    }

    @Test
    @DisplayName("A tapped, summoning-sick Sentry can activate regeneration")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent sentry = harness.addToBattlefieldAndReturn(player1, new SentryOfTheUnderworld());
        sentry.setSummoningSick(true);
        sentry.tap();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(sentry.getRegenerationShield()).isEqualTo(1);
        assertThat(sentry.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Regeneration prevents one destruction, taps Sentry, and clears marked damage")
    void regenerationPreventsOnlyOneDestruction() {
        Permanent sentry = addSentryReady(player1);
        sentry.setMarkedDamage(2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new HerosDownfall(), new HerosDownfall()));
        harness.addMana(player2, ManaColor.BLACK, 6);
        harness.castAndResolveInstant(player2, 0, sentry.getId());

        harness.assertOnBattlefield(player1, "Sentry of the Underworld");
        assertThat(sentry.isTapped()).isTrue();
        assertThat(sentry.getMarkedDamage()).isZero();
        assertThat(sentry.getRegenerationShield()).isZero();

        harness.castAndResolveInstant(player2, 0, sentry.getId());

        harness.assertNotOnBattlefield(player1, "Sentry of the Underworld");
        harness.assertInGraveyard(player1, "Sentry of the Underworld");
    }

    private Permanent addSentryReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new SentryOfTheUnderworld());
        perm.setSummoningSick(false);
        return perm;
    }
}
