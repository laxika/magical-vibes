package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SirenReaver.class})
class SirenReaverTest extends BaseCardTest {

    @Test
    @DisplayName("Costs {1} less to cast after attacking this turn")
    void costsLessAfterAttacking() {
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());

        harness.castFromHand(player1, new SirenReaver(), "{2}{U}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Cannot cast for the reduced cost without attacking this turn")
    void doesNotReduceWithoutAttacking() {
        assertThatThrownBy(() -> harness.castFromHand(player1, new SirenReaver(), "{2}{U}"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can cast for the full cost without attacking")
    void castsForFullCostWithoutAttacking() {
        harness.castFromHand(player1, new SirenReaver(), "{3}{U}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("An opponent's attack does not reduce the casting cost")
    void opponentsAttackDoesNotEnableRaid() {
        gd.playersDeclaredAttackersThisTurn.add(player2.getId());

        assertThatThrownBy(() -> harness.castFromHand(player1, new SirenReaver(), "{2}{U}"))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Siren Reaver");
    }

    @Test
    @DisplayName("Raid does not remove the blue mana requirement")
    void raidStillRequiresBlueMana() {
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());

        assertThatThrownBy(() -> harness.castFromHand(player1, new SirenReaver(), "{3}"))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Siren Reaver");
    }

    @Test
    @DisplayName("A Siren Reaver on the battlefield does not provide an additional reduction")
    void battlefieldCopyDoesNotAddAnotherReduction() {
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());
        harness.addToBattlefield(player1, new SirenReaver());
        harness.setHand(player1, List.of(new SirenReaver()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }
}
