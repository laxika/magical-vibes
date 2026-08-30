package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HumongulusTest extends BaseCardTest {

    @Test
    @DisplayName("Humongulus has hexproof")
    void hasHexproof() {
        Permanent humongulus = addReadyHumongulus(player1);

        assertThat(gqs.hasKeyword(gd, humongulus, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("Opponent cannot target Humongulus with a spell")
    void opponentCannotTargetWithSpell() {
        Permanent humongulus = addReadyHumongulus(player1);

        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.passPriority(player1);

        assertThatThrownBy(() -> gs.playCard(gd, player2, 0, 0, humongulus.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }

    @Test
    @DisplayName("Controller can target Humongulus with a spell")
    void controllerCanTargetWithSpell() {
        Permanent humongulus = addReadyHumongulus(player1);

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, humongulus.getId());

        assertThat(gd.stack).hasSize(1);
    }

    private Permanent addReadyHumongulus(com.github.laxika.magicalvibes.model.Player player) {
        Permanent humongulus = new Permanent(new Humongulus());
        humongulus.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(humongulus);
        return humongulus;
    }
}
