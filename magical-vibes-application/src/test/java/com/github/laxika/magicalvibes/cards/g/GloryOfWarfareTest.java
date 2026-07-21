package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GloryOfWarfareTest extends BaseCardTest {

    // ===== During your turn: +2/+0 =====

    @Test
    @DisplayName("Creatures you control get +2/+0 during your turn")
    void plusTwoZeroDuringYourTurn() {
        Permanent creature = addReadyCreature(player1);
        addGlory(player1);

        harness.forceActivePlayer(player1);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);     // 2 + 2
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2); // 2 + 0
    }

    // ===== During other turns: +0/+2 =====

    @Test
    @DisplayName("Creatures you control get +0/+2 during turns other than yours")
    void plusZeroTwoDuringOpponentTurn() {
        Permanent creature = addReadyCreature(player1);
        addGlory(player1);

        harness.forceActivePlayer(player2);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);     // 2 + 0
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4); // 2 + 2
    }

    // ===== Boost toggles with the active player =====

    @Test
    @DisplayName("Boost switches between +2/+0 and +0/+2 as the active player changes")
    void boostTogglesWithActivePlayer() {
        Permanent creature = addReadyCreature(player1);
        addGlory(player1);

        harness.forceActivePlayer(player1);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);

        harness.forceActivePlayer(player2);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);

        harness.forceActivePlayer(player1);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    // ===== Only affects the controller's creatures =====

    @Test
    @DisplayName("Does not boost the opponent's creatures")
    void doesNotBoostOpponentCreatures() {
        Permanent enemy = addReadyCreature(player2);
        addGlory(player1);

        harness.forceActivePlayer(player1);

        assertThat(gqs.getEffectivePower(gd, enemy)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, enemy)).isEqualTo(2);
    }

    // ===== Helpers =====

    private void addGlory(Player player) {
        gd.playerBattlefields.get(player.getId()).add(new Permanent(new GloryOfWarfare()));
    }

    private Permanent addReadyCreature(Player player) {
        Permanent perm = new Permanent(new com.github.laxika.magicalvibes.cards.g.GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
