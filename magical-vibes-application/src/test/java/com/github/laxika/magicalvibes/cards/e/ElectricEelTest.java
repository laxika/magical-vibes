package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(ElectricEel.class)
class ElectricEelTest extends BaseCardTest {

    @Test
    @DisplayName("When Electric Eel enters, it deals 1 damage to its controller")
    void enterTheBattlefieldDealsDamage() {
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castFromHand(player1, new ElectricEel(), "{U}");
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("Its ability gives it +2/+0 and deals 1 damage to its controller")
    void abilityBoostsAndDealsDamage() {
        Permanent eel = addCreatureReady(player1, new ElectricEel());
        harness.addMana(player1, ManaColor.RED, 2);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(eel.getPowerModifier()).isEqualTo(2);
        assertThat(eel.getToughnessModifier()).isZero();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("The ability's power boost wears off at end of turn")
    void abilityBoostWearsOffAtEndOfTurn() {
        Permanent eel = addCreatureReady(player1, new ElectricEel());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(eel.getPowerModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(eel.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("The ability requires two red mana")
    void abilityRequiresTwoRedMana() {
        addCreatureReady(player1, new ElectricEel());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    void abilityRequiresRedManaForBothSymbols() {
        addCreatureReady(player1, new ElectricEel());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
    @Test
    void abilityCanBeActivatedWithSummoningSickness() {
        harness.castFromHand(player1, new ElectricEel(), "{U}");
        resolveAllTriggers();

        Permanent eel = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.addMana(player1, ManaColor.RED, 2);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(eel.getPowerModifier()).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    void stackedActivationsResolveIndependentlyAndAccumulate() {
        Permanent eel = addCreatureReady(player1, new ElectricEel());
        harness.addMana(player1, ManaColor.RED, 4);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);

        assertThat(eel.getPowerModifier()).isZero();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);

        harness.passBothPriorities();

        assertThat(eel.getPowerModifier()).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);

        harness.passBothPriorities();

        assertThat(eel.getPowerModifier()).isEqualTo(4);
        assertThat(eel.getToughnessModifier()).isZero();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore);
    }

    @Test
    void abilityCanBeActivatedWhileTapped() {
        Permanent eel = addCreatureReady(player1, new ElectricEel());
        eel.tap();
        harness.addMana(player1, ManaColor.RED, 2);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(eel.isTapped()).isTrue();
        assertThat(eel.getPowerModifier()).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    void opponentsEnterTriggerDamagesOnlyItsControllerOnResolution() {
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.enterBattlefieldAndReturn(player2, new ElectricEel());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore - 1);
    }
}
