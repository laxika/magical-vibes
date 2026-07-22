package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TreeOfPerditionTest extends BaseCardTest {

    @Test
    @DisplayName("Exchange sets opponent life to toughness and toughness to old life total")
    void exchangeOpponentLifeAndToughness() {
        Permanent tree = addReadyTree(player1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(13);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gqs.getEffectiveToughness(gd, tree)).isEqualTo(20);
    }

    @Test
    @DisplayName("Exchange when opponent life is lower than toughness raises opponent life")
    void exchangeWhenOpponentLifeLowerThanToughness() {
        Permanent tree = addReadyTree(player1);
        gd.playerLifeTotals.put(player2.getId(), 5);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(13);
        assertThat(gqs.getEffectiveToughness(gd, tree)).isEqualTo(5);
    }

    @Test
    @DisplayName("Toughness override persists across turns")
    void toughnessPersistsAcrossTurns() {
        Permanent tree = addReadyTree(player1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectiveToughness(gd, tree)).isEqualTo(20);

        tree.resetModifiers();
        assertThat(gqs.getEffectiveToughness(gd, tree)).isEqualTo(20);
    }

    @Test
    @DisplayName("+1/+1 counters apply on top of exchanged toughness")
    void countersApplyOnTopOfExchangedToughness() {
        Permanent tree = addReadyTree(player1);
        tree.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(15);
        assertThat(gqs.getEffectiveToughness(gd, tree)).isEqualTo(22);
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetSelf() {
        addReadyTree(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }

    @Test
    @DisplayName("Activating ability taps Tree of Perdition")
    void activatingTapsTree() {
        Permanent tree = addReadyTree(player1);

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(tree.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate while summoning sick")
    void cannotActivateWhileSummoningSick() {
        Permanent tree = new Permanent(new TreeOfPerdition());
        tree.setSummoningSick(true);
        gd.playerBattlefields.get(player1.getId()).add(tree);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyTree(Player player) {
        Permanent perm = new Permanent(new TreeOfPerdition());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
