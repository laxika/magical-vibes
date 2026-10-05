package com.github.laxika.magicalvibes.cards.k;

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

@CardUsed(KravensCats.class)
class KravensCatsTest extends BaseCardTest {

    @Test
    @DisplayName("Pump ability grants +2/+2 until end of turn")
    void pumpAbilityGrantsBoost() {
        Permanent cats = addReadyCats(player1);
        addManaForAbility(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, cats)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, cats)).isEqualTo(4);
    }

    @Test
    @DisplayName("Pump ability can be activated only once each turn")
    void pumpAbilityOncePerTurn() {
        addReadyCats(player1);
        addManaForAbility(player1, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        Permanent cats = addReadyCats(player1);
        addManaForAbility(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, cats)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, cats)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, cats)).isEqualTo(2);
    }

    @Test
    @DisplayName("Activation limit applies before the first activation resolves")
    void cannotActivateAgainWhileAbilityIsOnStack() {
        Permanent cats = addReadyCats(player1);
        addManaForAbility(player1, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");

        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, cats)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, cats)).isEqualTo(4);
    }

    @Test
    @DisplayName("Each copy can activate its own ability once in the same turn")
    void separateCopiesHaveSeparateActivationLimits() {
        Permanent first = addReadyCats(player1);
        Permanent second = addReadyCats(player1);
        addManaForAbility(player1, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(2);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(4);
    }

    @Test
    @DisplayName("A tapped summoning-sick creature can activate the pump ability")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent cats = harness.addToBattlefieldAndReturn(player1, new KravensCats());
        cats.setSummoningSick(true);
        cats.setTapped(true);
        addManaForAbility(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, cats)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, cats)).isEqualTo(4);
        assertThat(cats.isTapped()).isTrue();
    }

    private Permanent addReadyCats(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new KravensCats());
        perm.setSummoningSick(false);
        return perm;
    }

    private void addManaForAbility(Player player) {
        addManaForAbility(player, 1);
    }

    private void addManaForAbility(Player player, int activations) {
        harness.addMana(player, ManaColor.GREEN, activations);
        harness.addMana(player, ManaColor.COLORLESS, activations * 2);
    }
}
