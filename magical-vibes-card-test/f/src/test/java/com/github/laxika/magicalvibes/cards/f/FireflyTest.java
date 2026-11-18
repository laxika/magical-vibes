package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Firefly.class})
class FireflyTest extends BaseCardTest {

    @Test
    @DisplayName("A tapped, summoning-sick Firefly can activate its ability")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent firefly = harness.addToBattlefieldAndReturn(player1, new Firefly());
        firefly.setSummoningSick(true);
        firefly.tap();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, firefly)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, firefly)).isZero();
        assertThat(firefly.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The boost applies on resolution and only to the activating Firefly")
    void boostsOnlySourceOnResolution() {
        Permanent other = addCreatureReady(player1, new Firefly());
        Permanent source = addCreatureReady(player1, new Firefly());
        Permanent opponent = addCreatureReady(player2, new Firefly());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 1, null, null);

        assertThat(PermanentGrants.powerBoostOf(gd, source)).isZero();

        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, source)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, source)).isZero();
        assertThat(PermanentGrants.powerBoostOf(gd, other)).isZero();
        assertThat(PermanentGrants.powerBoostOf(gd, opponent)).isZero();
    }

    @Test
    @DisplayName("Activating the ability gives +1/+0 until end of turn")
    void abilityBoostsPower() {
        Permanent firefly = addCreatureReady(player1, new Firefly());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, firefly)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, firefly)).isEqualTo(0);
    }

    @Test
    @DisplayName("Ability can be activated repeatedly for a cumulative boost")
    void abilityStacks() {
        Permanent firefly = addCreatureReady(player1, new Firefly());
        harness.addMana(player1, ManaColor.RED, 3);

        for (int i = 0; i < 3; i++) {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
        }

        assertThat(PermanentGrants.powerBoostOf(gd, firefly)).isEqualTo(3);
        assertThat(PermanentGrants.toughnessBoostOf(gd, firefly)).isEqualTo(0);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        Permanent firefly = addCreatureReady(player1, new Firefly());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(PermanentGrants.powerBoostOf(gd, firefly)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, firefly)).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot activate the ability without red mana")
    void cannotActivateWithoutMana() {
        addCreatureReady(player1, new Firefly());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Cannot pay the red activation cost with only colorless mana")
    void cannotActivateWithOnlyColorlessMana() {
        addCreatureReady(player1, new Firefly());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }
}
