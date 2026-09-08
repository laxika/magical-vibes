package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AbattoirGhoul;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MarshmistTitanTest extends BaseCardTest {

    @Test
    @DisplayName("Can be cast for its full cost with no black devotion")
    void canBeCastForFullCostWithNoBlackDevotion() {
        harness.setHand(player1, List.of(new MarshmistTitan()));
        addMana(player1, 6, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Costs one less for one black mana symbol among permanents you control")
    void costsOneLessForOneBlackDevotion() {
        harness.addToBattlefield(player1, new AbattoirGhoul());
        harness.setHand(player1, List.of(new MarshmistTitan()));
        addMana(player1, 5, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Does not count an opponent's black devotion")
    void doesNotCountOpponentsBlackDevotion() {
        harness.addToBattlefield(player2, new AbattoirGhoul());
        harness.setHand(player1, List.of(new MarshmistTitan()));
        addMana(player1, 5, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    private void addMana(Player player, int colorless, int black) {
        harness.addMana(player, ManaColor.COLORLESS, colorless);
        harness.addMana(player, ManaColor.BLACK, black);
    }
}
