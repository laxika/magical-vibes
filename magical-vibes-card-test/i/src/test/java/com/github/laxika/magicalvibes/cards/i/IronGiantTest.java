package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.c.Coeurl;
import com.github.laxika.magicalvibes.cards.g.Gaelicat;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IronGiant.class, Gaelicat.class, Coeurl.class})
class IronGiantTest extends BaseCardTest {

    @Test
    @DisplayName("Reach allows Iron Giant to block a flying creature")
    void reachAllowsBlockingFlyingCreature() {
        Permanent attacker = addCreatureReady(player1, new Gaelicat());
        Permanent giant = addCreatureReady(player2, new IronGiant());

        declareAttackersAndPrepareBlockers(player1, List.of(0));

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(giant);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(giant.getBlockingTargetIds()).contains(attacker.getId());
    }

    @Test
    @DisplayName("Vigilance keeps Iron Giant untapped when it attacks")
    void vigilanceKeepsItUntapped() {
        Permanent giant = addCreatureReady(player1, new IronGiant());

        declareAttackers(List.of(0));

        assertThat(giant.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Trample deals excess combat damage to the defending player")
    void trampleDealsExcessCombatDamage() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new IronGiant());
        Permanent blocker = addCreatureReady(player2, new Coeurl());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2,
                player2.getId(), 4));

        harness.assertLife(player2, 16);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }

    @Test
    @DisplayName("Reach does not stop a ground creature from blocking Iron Giant")
    void reachDoesNotGrantEvasion() {
        Permanent giant = addCreatureReady(player1, new IronGiant());
        Permanent blocker = addCreatureReady(player2, new Coeurl());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.getBlockingTargetIds()).contains(giant.getId());
    }

    @Test
    @DisplayName("Trample requires lethal damage to the blocker before player damage")
    void trampleRequiresLethalDamageBeforeOverflow() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new IronGiant());
        Permanent blocker = addCreatureReady(player2, new Coeurl());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 1,
                player2.getId(), 5)))
                .isInstanceOf(IllegalStateException.class);
        harness.assertLife(player2, 20);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2,
                player2.getId(), 4));

        harness.assertLife(player2, 16);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }
}
