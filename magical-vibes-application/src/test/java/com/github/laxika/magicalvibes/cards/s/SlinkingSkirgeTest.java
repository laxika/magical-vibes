package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GoliathBeetle;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SlinkingSkirge.class, GoliathBeetle.class})
class SlinkingSkirgeTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices as an activation cost and draws a card")
    void sacrificesAndDrawsCard() {
        harness.addToBattlefield(player1, new SlinkingSkirge());
        harness.setLibrary(player1, List.of(new GoliathBeetle()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Slinking Skirge");
        harness.assertInGraveyard(player1, "Slinking Skirge");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertInHand(player1, "Goliath Beetle");
    }

    @Test
    @DisplayName("Cannot activate without paying the full {2} cost")
    void requiresTwoMana() {
        harness.addToBattlefield(player1, new SlinkingSkirge());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        harness.assertOnBattlefield(player1, "Slinking Skirge");
        assertThat(gd.stack).isEmpty();
    }

}
