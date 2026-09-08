package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WelderAutomatonTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to each opponent without damaging its controller")
    void dealsDamageToEachOpponent() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent automaton = harness.addToBattlefieldAndReturn(player1, new WelderAutomaton());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(automaton), null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Requires three generic and one red mana")
    void requiresMana() {
        harness.addToBattlefield(player1, new WelderAutomaton());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
