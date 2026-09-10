package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.s.SkyshroudFalcon;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DreamProwler.class, SkyshroudFalcon.class})
class DreamProwlerTest extends BaseCardTest {

    @Test
    @DisplayName("Dream Prowler can't be blocked while attacking alone")
    void cantBeBlockedWhenAttackingAlone() {
        Permanent blocker = addCreatureReady(player2, new SkyshroudFalcon());
        addCreatureReady(player1, new SkyshroudFalcon());

        Permanent prowler = addCreatureReady(player1, new DreamProwler());
        prowler.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                        gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                        gd.playerBattlefields.get(player1.getId()).indexOf(prowler)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Dream Prowler can be blocked when attacking alongside another creature")
    void canBeBlockedWhenNotAttackingAlone() {
        Permanent blocker = addCreatureReady(player2, new SkyshroudFalcon());

        Permanent prowler = addCreatureReady(player1, new DreamProwler());
        prowler.setAttacking(true);

        Permanent companion = addCreatureReady(player1, new SkyshroudFalcon());
        companion.setAttacking(true);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(prowler))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Unblocked Dream Prowler deals combat damage when attacking alone")
    void dealsDamageWhenUnblockedAlone() {
        harness.setLife(player2, 20);

        Permanent prowler = addCreatureReady(player1, new DreamProwler());
        prowler.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }
}
