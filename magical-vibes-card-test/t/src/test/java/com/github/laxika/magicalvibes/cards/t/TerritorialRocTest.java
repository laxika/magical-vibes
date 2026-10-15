package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AerieBowmasters;
import com.github.laxika.magicalvibes.cards.s.StampedingElkHerd;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TerritorialRoc.class, StampedingElkHerd.class, AerieBowmasters.class})
class TerritorialRocTest extends BaseCardTest {

    @Test
    @DisplayName("Territorial Roc can't be blocked by a creature without flying")
    void cannotBeBlockedByGroundCreature() {
        Permanent blocker = addCreatureReady(player2, new StampedingElkHerd());

        Permanent attacker = addCreatureReady(player1, new TerritorialRoc());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);

        assertThatThrownBy(() -> gs.declareBlockers(
                gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canBeBlockedByFlyingCreature() {
        addCreatureReady(player1, new TerritorialRoc());
        Permanent blocker = addCreatureReady(player2, new TerritorialRoc());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void canBeBlockedByCreatureWithReach() {
        addCreatureReady(player1, new TerritorialRoc());
        Permanent blocker = addCreatureReady(player2, new AerieBowmasters());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void canBlockGroundCreature() {
        addCreatureReady(player1, new StampedingElkHerd());
        Permanent blocker = addCreatureReady(player2, new TerritorialRoc());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
