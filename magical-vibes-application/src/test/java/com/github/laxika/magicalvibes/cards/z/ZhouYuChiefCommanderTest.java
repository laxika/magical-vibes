package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.ShuFootSoldiers;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ZhouYuChiefCommander.class, Island.class, ShuFootSoldiers.class})
class ZhouYuChiefCommanderTest extends BaseCardTest {

    @Test
    @DisplayName("Zhou Yu can attack when defending player controls an Island")
    void canAttackWhenDefenderControlsIsland() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new Island());

        addCreatureReady(player1, new ZhouYuChiefCommander());
        declareAttackers(List.of(0));

        // Combat auto-advances; 8/8 unblocked deals 8 damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(12);
    }

    @Test
    @DisplayName("Zhou Yu cannot attack when defending player does not control an Island")
    void cannotAttackWhenDefenderDoesNotControlIsland() {
        addCreatureReady(player1, new ZhouYuChiefCommander());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Zhou Yu cannot attack when only the attacking player controls an Island")
    void cannotAttackWhenOnlyAttackingPlayerControlsIsland() {
        addCreatureReady(player1, new ZhouYuChiefCommander());
        harness.addToBattlefield(player1, new Island());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Zhou Yu can attack when defending player controls a tapped Island")
    void canAttackWhenDefenderControlsTappedIsland() {
        harness.setLife(player2, 20);
        harness.addToBattlefieldAndReturn(player2, new Island()).tap();
        addCreatureReady(player1, new ZhouYuChiefCommander());

        declareAttackers(List.of(0));

        harness.assertLife(player2, 12);
    }

    @Test
    @DisplayName("Islands in the defending player's hand and graveyard do not allow Zhou Yu to attack")
    void cannotAttackWhenIslandsAreOutsideBattlefield() {
        harness.setHand(player2, List.of(new Island()));
        harness.setGraveyard(player2, List.of(new Island()));
        addCreatureReady(player1, new ZhouYuChiefCommander());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Zhou Yu checks the defending player when player two attacks")
    void canAttackFromOtherPlayersBattlefield() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new Island());
        addCreatureReady(player2, new ZhouYuChiefCommander());

        declareAttackers(player2, List.of(0));

        harness.assertLife(player1, 12);
    }

    @Test
    @DisplayName("Zhou Yu can block without either player controlling an Island")
    void canBlockWithoutIslands() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new ShuFootSoldiers());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new ZhouYuChiefCommander());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
        resolveCombat();
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Shu Foot Soldiers");
        harness.assertOnBattlefield(player2, "Zhou Yu, Chief Commander");
    }
}
