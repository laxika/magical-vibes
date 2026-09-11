package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SulfurousSprings.class, SamiteHealer.class})
class SulfurousSpringsTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping for colorless mana adds {C} and deals no damage")
    void tapForColorlessMana() {
        harness.setLife(player1, 20);
        Permanent springs = harness.addToBattlefieldAndReturn(player1, new SulfurousSprings());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(springs.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Tapping for black mana adds {B} and deals 1 damage to controller")
    void tapForBlackMana() {
        harness.setLife(player1, 20);
        Permanent springs = harness.addToBattlefieldAndReturn(player1, new SulfurousSprings());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(springs.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Tapping for red mana adds {R} and deals 1 damage to controller")
    void tapForRedMana() {
        harness.setLife(player1, 20);
        Permanent springs = harness.addToBattlefieldAndReturn(player1, new SulfurousSprings());

        harness.activateAbility(player1, 0, 2, null, null);

        assertThat(springs.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Cannot activate when already tapped")
    void cannotActivateWhileTapped() {
        harness.addToBattlefield(player1, new SulfurousSprings());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Colored mana abilities are mana abilities and do not use the stack")
    void coloredManaAbilitiesDoNotUseStack() {
        harness.addToBattlefield(player1, new SulfurousSprings());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Colored mana abilities damage only their controller")
    void coloredManaDamagesOnlyController() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 17);
        harness.addToBattlefield(player1, new SulfurousSprings());

        harness.activateAbility(player1, 0, 1, null, null);

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Pain-land damage can be prevented while mana is still added")
    void painLandDamageCanBePrevented() {
        harness.setLife(player1, 20);
        addCreatureReady(player1, new SamiteHealer());
        harness.addToBattlefield(player1, new SulfurousSprings());

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, 1, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        harness.assertLife(player1, 20);
        assertThat(gd.playerDamagePreventionShields.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
