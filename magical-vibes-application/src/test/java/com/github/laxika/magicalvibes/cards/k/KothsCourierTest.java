package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KothsCourier.class, Forest.class, GrizzlyBears.class})
class KothsCourierTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Koth's Courier puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new KothsCourier()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getCard()).isInstanceOf(KothsCourier.class);
    }

    @Test
    @DisplayName("Koth's Courier cannot be blocked when defending player controls a Forest")
    void cannotBeBlockedWhenDefenderControlsForest() {
        harness.addToBattlefield(player2, new Forest());

        Permanent blockerPerm = addCreatureReady(player2, new GrizzlyBears());

        Permanent atkPerm = addCreatureReady(player1, new KothsCourier());
        atkPerm.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(atkPerm);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Koth's Courier can be blocked when defending player does not control a Forest")
    void canBeBlockedWhenDefenderDoesNotControlForest() {
        Permanent blockerPerm = addCreatureReady(player2, new GrizzlyBears());

        Permanent atkPerm = addCreatureReady(player1, new KothsCourier());
        atkPerm.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(atkPerm);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blockerPerm.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Unblocked Koth's Courier deals 2 damage to defending player")
    void dealsTwoDamageWhenUnblocked() {
        harness.setLife(player2, 20);

        Permanent atkPerm = addCreatureReady(player1, new KothsCourier());
        atkPerm.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("A Forest controlled by the attacker does not prevent blocking")
    void attackersForestDoesNotPreventBlocking() {
        harness.addToBattlefield(player1, new Forest());
        Permanent attacker = addCreatureReady(player1, new KothsCourier());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new KothsCourier());
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A tapped Forest still prevents blocking Koth's Courier")
    void tappedForestPreventsBlocking() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        forest.tap();
        addCreatureReady(player2, new KothsCourier());
        Permanent attacker = addCreatureReady(player1, new KothsCourier());
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(1, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }
}
