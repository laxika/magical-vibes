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

    @Test
    @DisplayName("Colored mana pays the generic cost and life is gained only on resolution")
    void coloredManaPaysCostAndLifeGainUsesStack() {
        Permanent tower = harness.addToBattlefieldAndReturn(player1, new TowerOfEons());
        harness.setLife(player1, 12);
        harness.setLife(player2, 17);
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.activateAbility(player1, 0, null, null);

        assertThat(tower.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(12);

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Tower can be activated during the opponent's turn and benefits its controller")
    void activatesDuringOpponentsTurn() {
        harness.addToBattlefield(player2, new TowerOfEons());
        harness.setLife(player1, 20);
        harness.setLife(player2, 9);
        harness.forceActivePlayer(player1);
        harness.addMana(player2, ManaColor.COLORLESS, 8);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }
}
