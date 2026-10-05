package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MagisterSphinx.class})
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
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("ETB raises a low-life target to 10 (a gain)")
    void etbRaisesLowLifeTargetTo10() {
        harness.setHand(player1, List.of(new MagisterSphinx()));
        addManaCost(player1);
        harness.setLife(player2, 3);

        harness.castCreature(player1, 0, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("ETB touches only the targeted player, not the other one")
    void etbLeavesTheUntargetedPlayerAlone() {
        harness.setHand(player1, List.of(new MagisterSphinx()));
        addManaCost(player1);
        harness.setLife(player1, 25);
        harness.setLife(player2, 20);

        harness.castCreature(player1, 0, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(10);
        assertThat(gd.getLife(player1.getId())).isEqualTo(25);
    }

    @Test
    @DisplayName("ETB can target its own controller")
    void etbCanTargetSelf() {
        harness.setHand(player1, List.of(new MagisterSphinx()));
        addManaCost(player1);
        harness.setLife(player1, 25);

        harness.castCreature(player1, 0, 0, player1.getId());
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("A target already at 10 neither gains nor loses life")
    void targetAlreadyAt10DoesNotGainLife() {
        harness.setHand(player1, List.of(new MagisterSphinx()));
        addManaCost(player1);
        harness.setLife(player2, 10);

        harness.castCreature(player1, 0, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(10);
        assertThat(gd.lifeGainedThisTurn.getOrDefault(player2.getId(), 0)).isZero();
        assertThat(gd.lifeLostThisTurn.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("The trigger uses the target's life total at resolution")
    void usesLifeTotalAtResolution() {
        harness.setHand(player1, List.of(new MagisterSphinx()));
        addManaCost(player1);
        harness.setLife(player2, 20);

        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.stack).hasSize(1);
        harness.setLife(player2, 3);
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(10);
        assertThat(gd.lifeGainedThisTurn.getOrDefault(player2.getId(), 0)).isEqualTo(7);
    }
}
