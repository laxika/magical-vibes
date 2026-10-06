package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AxebaneStag;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SelesnyaSentry.class, AxebaneStag.class})
class SelesnyaSentryTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the activated ability grants a regeneration shield")
    void resolvingRegenGrantsShield() {
        addCreatureReady(player1, new SelesnyaSentry());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent sentry = findPermanent(player1, "Selesnya Sentry");
        assertThat(sentry.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Regeneration shield saves Selesnya Sentry from lethal combat damage")
    void regenSavesFromLethalCombat() {
        Permanent perm = addCreatureReady(player1, new SelesnyaSentry());
        perm.setRegenerationShield(1);
        perm.setBlocking(true);
        perm.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new AxebaneStag());
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Selesnya Sentry");
        Permanent sentry = findPermanent(player1, "Selesnya Sentry");
        assertThat(sentry.isTapped()).isTrue();
        assertThat(sentry.getRegenerationShield()).isEqualTo(0);
    }

    @Test
    @DisplayName("Selesnya Sentry dies without a regeneration shield")
    void diesWithoutRegenShield() {
        Permanent perm = addCreatureReady(player1, new SelesnyaSentry());
        perm.setBlocking(true);
        perm.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new AxebaneStag());
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertNotOnBattlefield(player1, "Selesnya Sentry");
        harness.assertInGraveyard(player1, "Selesnya Sentry");
    }

    @Test
    @DisplayName("Tapped and summoning-sick Selesnya Sentry can activate regeneration")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent sentry = harness.addToBattlefieldAndReturn(player1, new SelesnyaSentry());
        sentry.setSummoningSick(true);
        sentry.tap();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, null);
        assertThat(sentry.getRegenerationShield()).isZero();
        harness.passBothPriorities();

        assertThat(sentry.getRegenerationShield()).isEqualTo(1);
        assertThat(sentry.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Separate activations create separate shields without tapping the creature")
    void multipleActivationsCreateMultipleShields() {
        Permanent sentry = addCreatureReady(player1, new SelesnyaSentry());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(sentry.getRegenerationShield()).isEqualTo(2);
        assertThat(sentry.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Regeneration requires the green mana in its cost")
    void cannotActivateWithoutGreenMana() {
        Permanent sentry = addCreatureReady(player1, new SelesnyaSentry());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(sentry.getRegenerationShield()).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
