package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(TowerOfEons.class)
class TowerOfEonsTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {8} and tapping Tower of Eons gains 10 life")
    void gainsTenLife() {
        Permanent tower = harness.addToBattlefieldAndReturn(player1, new TowerOfEons());
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(30);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(tower.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate Tower of Eons without enough mana")
    void requiresEightMana() {
        Permanent tower = harness.addToBattlefieldAndReturn(player1, new TowerOfEons());
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(tower.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate Tower of Eons while it is already tapped")
    void cannotActivateWhileTapped() {
        Permanent tower = harness.addToBattlefieldAndReturn(player1, new TowerOfEons());
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        tower.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(8);
        assertThat(gd.stack).isEmpty();
    }
}
