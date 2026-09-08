package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Warthog.class, Swamp.class, GrizzlyBears.class})
class WarthogTest extends BaseCardTest {

    @Test
    @DisplayName("Warthog cannot be blocked when defending player controls a Swamp")
    void cannotBeBlockedWhenDefenderControlsSwamp() {
        harness.addToBattlefield(player2, new Swamp());

        Permanent blockerPerm = addCreatureReady(player2, new GrizzlyBears());

        Permanent atkPerm = addCreatureReady(player1, new Warthog());
        atkPerm.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(atkPerm);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Warthog can be blocked when defending player does not control a Swamp")
    void canBeBlockedWhenDefenderDoesNotControlSwamp() {
        Permanent blockerPerm = addCreatureReady(player2, new GrizzlyBears());

        Permanent atkPerm = addCreatureReady(player1, new Warthog());
        atkPerm.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(atkPerm);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blockerPerm.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Warthog can be blocked when only its controller controls a Swamp")
    void canBeBlockedWhenOnlyAttackerControlsSwamp() {
        harness.addToBattlefield(player1, new Swamp());
        Permanent blockerPerm = addCreatureReady(player2, new GrizzlyBears());
        Permanent atkPerm = addCreatureReady(player1, new Warthog());
        atkPerm.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(atkPerm);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blockerPerm.isBlocking()).isTrue();
    }
}
