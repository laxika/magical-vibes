package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BullHippo.class, GrizzlyBears.class, Island.class, Forest.class})
class BullHippoTest extends BaseCardTest {

    @Test
    @DisplayName("Bull Hippo cannot be blocked when defending player controls an Island")
    void cannotBeBlockedWhenDefenderControlsIsland() {
        harness.addToBattlefield(player2, new Island());

        Permanent blockerPerm = addCreatureReady(player2, new GrizzlyBears());
        Permanent atkPerm = addCreatureReady(player1, new BullHippo());
        declareAttackers(List.of(0));
        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(atkPerm);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Bull Hippo can be blocked when defending player does not control an Island")
    void canBeBlockedWhenDefenderDoesNotControlIsland() {
        Permanent blockerPerm = addCreatureReady(player2, new GrizzlyBears());
        Permanent atkPerm = addCreatureReady(player1, new BullHippo());
        declareAttackers(List.of(0));
        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(atkPerm);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blockerPerm.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Bull Hippo can be blocked when only the attacking player controls an Island")
    void canBeBlockedWhenOnlyAttackerControlsIsland() {
        Permanent blockerPerm = addCreatureReady(player2, new GrizzlyBears());
        Permanent atkPerm = addCreatureReady(player1, new BullHippo());
        harness.addToBattlefield(player1, new Island());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(atkPerm);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blockerPerm.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Bull Hippo can be blocked when defending player controls only a Forest")
    void canBeBlockedWithForestOnly() {
        harness.addToBattlefield(player2, new Forest());
        Permanent blockerPerm = addCreatureReady(player2, new GrizzlyBears());
        Permanent atkPerm = addCreatureReady(player1, new BullHippo());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(atkPerm);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blockerPerm.isBlocking()).isTrue();
    }
}
