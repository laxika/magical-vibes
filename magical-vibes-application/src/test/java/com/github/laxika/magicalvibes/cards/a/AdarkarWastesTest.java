package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.s.SamiteHealer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AdarkarWastes.class, SamiteHealer.class})
class AdarkarWastesTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping for colorless adds {C} and does not deal damage")
    void tapForColorlessAddsManaNoDamage() {
        harness.setLife(player1, 20);
        Permanent wastes = harness.addToBattlefieldAndReturn(player1, new AdarkarWastes());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        harness.assertLife(player1, 20);
        assertThat(wastes.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Tapping for white adds {W} and deals 1 damage to controller")
    void tapForWhiteAddsManaAndDealsDamage() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new AdarkarWastes());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        harness.assertLife(player1, 19);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Tapping for blue adds {U} and deals 1 damage to controller")
    void tapForBlueAddsManaAndDealsDamage() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new AdarkarWastes());

        harness.activateAbility(player1, 0, 2, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        harness.assertLife(player1, 19);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Pain-land damage is dealt to the ability's controller")
    void painLandDamagesItsController() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new AdarkarWastes());

        harness.activateAbility(player2, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate ability when already tapped")
    void cannotActivateWhenTapped() {
        Permanent wastes = harness.addToBattlefieldAndReturn(player1, new AdarkarWastes());
        wastes.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Multiple pain land activations accumulate damage")
    void cumulativeDamageAcrossActivations() {
        harness.setLife(player1, 20);
        Permanent wastes = harness.addToBattlefieldAndReturn(player1, new AdarkarWastes());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.assertLife(player1, 19);

        wastes.untap();
        harness.activateAbility(player1, 0, 2, null, null);
        harness.assertLife(player1, 18);

        wastes.untap();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Pain-land damage can be prevented while mana is still added")
    void painLandDamageCanBePrevented() {
        harness.setLife(player1, 20);
        addCreatureReady(player1, new SamiteHealer());
        harness.addToBattlefield(player1, new AdarkarWastes());

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, 1, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        harness.assertLife(player1, 20);
        assertThat(gd.playerDamagePreventionShields.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
