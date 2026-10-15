package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AvenInitiate;
import com.github.laxika.magicalvibes.cards.d.DuneBeetle;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SlitherBlade.class, DuneBeetle.class, AvenInitiate.class})
class SlitherBladeTest extends BaseCardTest {

    @Test
    @DisplayName("Slither Blade cannot be blocked by a ground creature")
    void cannotBeBlockedByGroundCreature() {
        addCreatureReady(player2, new DuneBeetle());

        Permanent atkPerm = addCreatureReady(player1, new SlitherBlade());
        atkPerm.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Unblocked Slither Blade deals 1 damage to defending player")
    void dealsDamageWhenUnblocked() {
        harness.setLife(player2, 20);

        Permanent atkPerm = addCreatureReady(player1, new SlitherBlade());
        atkPerm.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Slither Blade cannot be blocked by a flying creature")
    void cannotBeBlockedByFlyingCreature() {
        addCreatureReady(player2, new AvenInitiate());
        Permanent attacker = addCreatureReady(player1, new SlitherBlade());
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Slither Blade can block a ground creature")
    void canBlockGroundCreature() {
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player1, new DuneBeetle());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new SlitherBlade());
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertLife(player2, 20);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
    }
}
