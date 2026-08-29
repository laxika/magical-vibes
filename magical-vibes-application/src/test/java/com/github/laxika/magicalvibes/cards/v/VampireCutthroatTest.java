package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VampireCutthroatTest extends BaseCardTest {

    @Test
    @DisplayName("Vampire Cutthroat cannot be blocked by a creature with greater power")
    void cannotBeBlockedByGreaterPower() {
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent cutthroat = addCreatureReady(player1, new VampireCutthroat());
        cutthroat.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(cutthroat)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("skulk");
    }

    @Test
    @DisplayName("Vampire Cutthroat can be blocked by a creature with equal power")
    void canBeBlockedByEqualPower() {
        Permanent blocker = addCreatureReady(player2, new VampireCutthroat());
        Permanent cutthroat = addCreatureReady(player1, new VampireCutthroat());
        cutthroat.setAttacking(true);

        prepareDeclareBlockers();

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(cutthroat)))))
                .doesNotThrowAnyException();
        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Vampire Cutthroat's lifelink gains life when it deals combat damage")
    void lifelinkGainsLife() {
        Permanent cutthroat = addCreatureReady(player1, new VampireCutthroat());
        cutthroat.setAttacking(true);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        resolveCombat(player1);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }
}
