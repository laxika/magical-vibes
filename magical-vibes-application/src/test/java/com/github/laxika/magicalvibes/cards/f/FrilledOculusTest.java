package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FrilledOculus.class})
class FrilledOculusTest extends BaseCardTest {

    @Test
    @DisplayName("Pump ability grants +2/+2 until end of turn")
    void pumpAbilityGrantsBoost() {
        Permanent oculus = addCreatureReady(player1, new FrilledOculus());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, oculus)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, oculus)).isEqualTo(5);
    }

    @Test
    @DisplayName("Pump ability can be activated only once each turn")
    void pumpAbilityOncePerTurn() {
        addCreatureReady(player1, new FrilledOculus());
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        Permanent oculus = addCreatureReady(player1, new FrilledOculus());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, oculus)).isEqualTo(3);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, oculus)).isEqualTo(1);
    }

    @Test
    @DisplayName("Activation limit applies before the first activation resolves")
    void cannotActivateAgainWhileAbilityIsOnStack() {
        addCreatureReady(player1, new FrilledOculus());
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Each Oculus has its own activation limit")
    void differentCopiesCanEachActivate() {
        Permanent first = addCreatureReady(player1, new FrilledOculus());
        Permanent second = addCreatureReady(player1, new FrilledOculus());
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(3);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(5);
    }

    @Test
    @DisplayName("Ability can be activated again on the opponent's turn")
    void activationLimitResetsOnNextTurn() {
        Permanent oculus = addCreatureReady(player1, new FrilledOculus());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(gqs.getEffectivePower(gd, oculus)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, oculus)).isEqualTo(3);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, oculus)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, oculus)).isEqualTo(5);
    }

    @Test
    @DisplayName("Tapped, summoning-sick Oculus can activate its ability")
    void activationDoesNotRequireTapOrHaste() {
        Permanent oculus = harness.addToBattlefieldAndReturn(player1, new FrilledOculus());
        oculus.setSummoningSick(true);
        oculus.setTapped(true);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, oculus)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, oculus)).isEqualTo(5);
    }

    @Test
    @DisplayName("Ability requires green mana and a failed activation does not use the limit")
    void requiresGreenMana() {
        Permanent oculus = addCreatureReady(player1, new FrilledOculus());
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, oculus)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, oculus)).isEqualTo(5);
    }
}
