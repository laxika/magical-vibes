package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoliathSphinx.class, GnarlidPack.class, GrapplerSpider.class})
class GoliathSphinxTest extends BaseCardTest {

    @Test
    @DisplayName("Goliath Sphinx cannot be blocked by a creature without flying")
    void cannotBeBlockedByCreatureWithoutFlying() {
        Permanent attacker = addCreatureReady(player1, new GoliathSphinx());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GnarlidPack());

        prepareDeclareBlockers(player1);

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    @DisplayName("Goliath Sphinx can be blocked by a creature with flying")
    void canBeBlockedByCreatureWithFlying() {
        Permanent attacker = addCreatureReady(player1, new GoliathSphinx());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GoliathSphinx());

        prepareDeclareBlockers(player1);

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);

        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Goliath Sphinx can be blocked by a creature with reach")
    void canBeBlockedByCreatureWithReach() {
        Permanent attacker = addCreatureReady(player1, new GoliathSphinx());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrapplerSpider());

        prepareDeclareBlockers(player1);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Goliath Sphinx can block a creature without flying")
    void canBlockCreatureWithoutFlying() {
        Permanent attacker = addCreatureReady(player1, new GnarlidPack());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GoliathSphinx());

        prepareDeclareBlockers(player1);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
