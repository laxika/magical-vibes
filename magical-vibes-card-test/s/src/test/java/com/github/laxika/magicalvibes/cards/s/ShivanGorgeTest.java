package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(ShivanGorge.class)
class ShivanGorgeTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping for mana adds one colorless mana")
    void tapsForColorless() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new ShivanGorge());

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Paying the activated ability deals 1 damage to each opponent")
    void activatedAbilityDamagesEachOpponent() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new ShivanGorge());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        harness.assertLife(player1, 20);
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activated ability requires red mana")
    void activatedAbilityRequiresRedMana() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new ShivanGorge());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(land.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
    }

    @Test
    @DisplayName("Activated ability requires two generic mana")
    void activatedAbilityRequiresTwoGenericMana() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new ShivanGorge());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(land.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    @Test
    @DisplayName("Damage uses the stack and tapping for its cost does not produce mana")
    void damageWaitsForResolutionAndConsumesMana() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new ShivanGorge());
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);

        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("A Gorge tapped for mana cannot also activate its damage ability")
    void tappedLandCannotActivateDamageAbility() {
        harness.addToBattlefield(player1, new ShivanGorge());
        harness.tapPermanent(player1, 0);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The other player's Gorge damages its opponent rather than its controller")
    void otherControllerDamagesItsOpponent() {
        harness.addToBattlefield(player2, new ShivanGorge());
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.activateAbility(player2, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
    }
}
