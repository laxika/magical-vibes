package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MagisterSphinxTest extends BaseCardTest {

    private void addManaCost(com.github.laxika.magicalvibes.model.Player player) {
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.addMana(player, ManaColor.BLUE, 1);
        harness.addMana(player, ManaColor.BLACK, 1);
        harness.addMana(player, ManaColor.COLORLESS, 4);
    }

    @Test
    @DisplayName("ETB sets target opponent's life total to 10 (a loss)")
    void etbSetsOpponentLifeTo10() {
        harness.setHand(player1, List.of(new MagisterSphinx()));
        addManaCost(player1);
        harness.setLife(player2, 20);

        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.getLife(player2.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("ETB raises a low-life target to 10 (a gain)")
    void etbRaisesLowLifeTargetTo10() {
        harness.setHand(player1, List.of(new MagisterSphinx()));
        addManaCost(player1);
        harness.setLife(player2, 3);

        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.getLife(player2.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("ETB can target its own controller")
    void etbCanTargetSelf() {
        harness.setHand(player1, List.of(new MagisterSphinx()));
        addManaCost(player1);
        harness.setLife(player1, 25);

        harness.castCreature(player1, 0, 0, player1.getId());
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.getLife(player1.getId())).isEqualTo(10);
    }
}
