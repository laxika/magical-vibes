package com.github.laxika.magicalvibes.cards.m;

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

@CardUsed({MoltenRavager.class})
class MoltenRavagerTest extends BaseCardTest {

    @Test
    @DisplayName("{R}: Molten Ravager gets +1/+0 until end of turn")
    void pumpAbilityBoostsPower() {
        Permanent ravager = addCreatureReady(player1, new MoltenRavager());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, ravager)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, ravager)).isEqualTo(0);
    }

    @Test
    @DisplayName("Pump wears off at end of turn")
    void pumpWearsOffAtEndOfTurn() {
        Permanent ravager = addCreatureReady(player1, new MoltenRavager());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(PermanentGrants.powerBoostOf(gd, ravager)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, ravager)).isEqualTo(0);
    }

    @Test
    @DisplayName("Repeated activations use the stack and boost only their source")
    void repeatedActivationsBoostOnlyTheirSource() {
        Permanent ravager = addCreatureReady(player1, new MoltenRavager());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new MoltenRavager());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new MoltenRavager());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        assertThat(PermanentGrants.powerBoostOf(gd, ravager)).isZero();

        harness.passBothPriorities();
        assertThat(PermanentGrants.powerBoostOf(gd, ravager)).isEqualTo(1);
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, ravager)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, ravager)).isZero();
        assertThat(ravager.isTapped()).isFalse();
        assertThat(PermanentGrants.powerBoostOf(gd, other)).isZero();
        assertThat(PermanentGrants.powerBoostOf(gd, opposing)).isZero();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();
        assertThat(PermanentGrants.powerBoostOf(gd, ravager)).isZero();
    }

    @Test
    @DisplayName("The ability works while tapped and summoning sick")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent ravager = harness.addToBattlefieldAndReturn(player1, new MoltenRavager());
        ravager.setSummoningSick(true);
        ravager.tap();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, ravager)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, ravager)).isZero();
        assertThat(ravager.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Each activation consumes one red mana")
    void eachActivationRequiresRedMana() {
        Permanent ravager = addCreatureReady(player1, new MoltenRavager());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(gd.stack).isEmpty();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(PermanentGrants.powerBoostOf(gd, ravager)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

}
