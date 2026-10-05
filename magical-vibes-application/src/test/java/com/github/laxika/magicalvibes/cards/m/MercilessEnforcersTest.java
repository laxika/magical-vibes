package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MercilessEnforcers.class})
class MercilessEnforcersTest extends BaseCardTest {

    @Test
    @DisplayName("The ability deals 1 damage to each opponent")
    void abilityDealsDamageToEachOpponent() {
        Permanent enforcers = addCreatureReady(player1, new MercilessEnforcers());
        harness.setLife(player2, 20);
        addAbilityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(enforcers.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The ability can be activated while the creature is tapped")
    void abilityDoesNotRequireTapping() {
        Permanent enforcers = addCreatureReady(player1, new MercilessEnforcers());
        enforcers.tap();
        addAbilityMana();

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("The ability requires four mana including a black mana")
    void cannotActivateWithoutAbilityCost() {
        addCreatureReady(player1, new MercilessEnforcers());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability damage gains life through lifelink")
    void abilityDamageGainsLife() {
        addCreatureReady(player1, new MercilessEnforcers());
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        addAbilityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 11);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("The ability can be activated with summoning sickness")
    void abilityWorksWithSummoningSickness() {
        Permanent enforcers = harness.addToBattlefieldAndReturn(player1, new MercilessEnforcers());
        enforcers.setSummoningSick(true);
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        addAbilityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 11);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Four colorless mana cannot pay the black requirement")
    void cannotActivateWithoutBlackMana() {
        addCreatureReady(player1, new MercilessEnforcers());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Multiple activations each deal damage and gain life")
    void multipleActivationsResolveIndependently() {
        addCreatureReady(player1, new MercilessEnforcers());
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 12);
        harness.assertLife(player2, 18);
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
