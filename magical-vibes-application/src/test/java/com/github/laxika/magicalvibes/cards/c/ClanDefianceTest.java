package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClanDefianceTest extends BaseCardTest {

    // Modes: 0 = X damage to creature with flying, 1 = X damage to creature without flying,
    //        2 = X damage to player or planeswalker.

    private void giveMana(int generic) {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, generic);
    }

    @Test
    @DisplayName("Flying mode: deals X damage to target creature with flying")
    void flyingModeDealsXDamage() {
        Permanent angel = addCreatureReady(player2, new SerraAngel());
        harness.setHand(player1, List.of(new ClanDefiance()));
        giveMana(3);

        harness.castModalSorceryWithModesForX(player1, 0, 1, 3, new int[]{0}, 3, List.of(angel.getId()));
        harness.passBothPriorities();

        assertThat(angel.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Non-flying mode: deals X damage to target creature without flying")
    void nonFlyingModeDealsXDamage() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ClanDefiance()));
        giveMana(1);

        harness.castModalSorceryWithModesForX(player1, 0, 1, 3, new int[]{1}, 1, List.of(bears.getId()));
        harness.passBothPriorities();

        assertThat(bears.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Player mode: deals X damage to target player")
    void playerModeDealsXDamage() {
        harness.setHand(player1, List.of(new ClanDefiance()));
        giveMana(4);

        harness.castModalSorceryWithModesForX(player1, 0, 1, 3, new int[]{2}, 4, List.of(player2.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("All three modes: each chosen target takes X damage")
    void allThreeModesResolve() {
        Permanent angel = addCreatureReady(player2, new SerraAngel());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ClanDefiance()));
        giveMana(2);

        harness.castModalSorceryWithModesForX(player1, 0, 1, 3, new int[]{0, 1, 2}, 2,
                List.of(angel.getId(), bears.getId(), player2.getId()));
        harness.passBothPriorities();

        assertThat(angel.getMarkedDamage()).isEqualTo(2);
        assertThat(bears.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Flying mode cannot target a creature without flying")
    void flyingModeRejectsGroundCreature() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ClanDefiance()));
        giveMana(2);

        assertThatThrownBy(() -> harness.castModalSorceryWithModesForX(
                player1, 0, 1, 3, new int[]{0}, 2, List.of(bears.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Non-flying mode cannot target a creature with flying")
    void nonFlyingModeRejectsFlier() {
        Permanent angel = addCreatureReady(player2, new SerraAngel());
        harness.setHand(player1, List.of(new ClanDefiance()));
        giveMana(2);

        assertThatThrownBy(() -> harness.castModalSorceryWithModesForX(
                player1, 0, 1, 3, new int[]{1}, 2, List.of(angel.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
