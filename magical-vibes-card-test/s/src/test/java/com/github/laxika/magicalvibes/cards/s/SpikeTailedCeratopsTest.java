package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.i.IxallisKeeper;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpikeTailedCeratops.class, IxallisKeeper.class})
class SpikeTailedCeratopsTest extends BaseCardTest {

    @Test
    @DisplayName("Each Ceratops can independently block two attackers")
    void eachCeratopsCanBlockTwoAttackers() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new SpikeTailedCeratops());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new SpikeTailedCeratops());
        for (int i = 0; i < 4; i++) {
            Permanent attacker = harness.addToBattlefieldAndReturn(player1, new IxallisKeeper());
            attacker.setSummoningSick(false);
            attacker.setAttacking(true);
        }

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(0, 1),
                new BlockerAssignment(1, 2),
                new BlockerAssignment(1, 3)
        ));

        assertThat(first.getBlockingTargets()).containsExactlyInAnyOrder(0, 1);
        assertThat(second.getBlockingTargets()).containsExactlyInAnyOrder(2, 3);
    }

    @Test
    @DisplayName("Spike-Tailed Ceratops can block two attackers")
    void canBlockTwoAttackers() {
        SpikeTailedCeratops card = new SpikeTailedCeratops();
        Permanent ceratopsPerm = harness.addToBattlefieldAndReturn(player2, card);
        ceratopsPerm.setSummoningSick(false);

        int ceratopsIdx = gd.playerBattlefields.get(player2.getId()).indexOf(ceratopsPerm);

        IxallisKeeper atk1 = new IxallisKeeper();
        Permanent atkPerm1 = harness.addToBattlefieldAndReturn(player1, atk1);
        atkPerm1.setSummoningSick(false);
        atkPerm1.setAttacking(true);

        IxallisKeeper atk2 = new IxallisKeeper();
        Permanent atkPerm2 = harness.addToBattlefieldAndReturn(player1, atk2);
        atkPerm2.setSummoningSick(false);
        atkPerm2.setAttacking(true);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(ceratopsIdx, 0),
                new BlockerAssignment(ceratopsIdx, 1)
        ));

        assertThat(ceratopsPerm.isBlocking()).isTrue();
        assertThat(ceratopsPerm.getBlockingTargets()).containsExactlyInAnyOrder(0, 1);
    }

    @Test
    @DisplayName("Spike-Tailed Ceratops cannot block three attackers")
    void cannotBlockThreeAttackers() {
        SpikeTailedCeratops card = new SpikeTailedCeratops();
        Permanent ceratopsPerm = harness.addToBattlefieldAndReturn(player2, card);
        ceratopsPerm.setSummoningSick(false);

        int ceratopsIdx = gd.playerBattlefields.get(player2.getId()).indexOf(ceratopsPerm);

        for (int i = 0; i < 3; i++) {
            IxallisKeeper atk = new IxallisKeeper();
            Permanent atkPerm = harness.addToBattlefieldAndReturn(player1, atk);
            atkPerm.setSummoningSick(false);
            atkPerm.setAttacking(true);
        }

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(ceratopsIdx, 0),
                new BlockerAssignment(ceratopsIdx, 1),
                new BlockerAssignment(ceratopsIdx, 2)
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("too many times");
    }

    @Test
    @DisplayName("Spike-Tailed Ceratops does not grant additional blocks to other creatures")
    void doesNotGrantAdditionalBlocksToOthers() {
        SpikeTailedCeratops ceratops = new SpikeTailedCeratops();
        Permanent ceratopsPerm = harness.addToBattlefieldAndReturn(player2, ceratops);
        ceratopsPerm.setSummoningSick(false);

        IxallisKeeper blocker = new IxallisKeeper();
        Permanent blockerPerm = harness.addToBattlefieldAndReturn(player2, blocker);
        blockerPerm.setSummoningSick(false);

        int bearIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);

        IxallisKeeper atk1 = new IxallisKeeper();
        Permanent atkPerm1 = harness.addToBattlefieldAndReturn(player1, atk1);
        atkPerm1.setSummoningSick(false);
        atkPerm1.setAttacking(true);

        IxallisKeeper atk2 = new IxallisKeeper();
        Permanent atkPerm2 = harness.addToBattlefieldAndReturn(player1, atk2);
        atkPerm2.setSummoningSick(false);
        atkPerm2.setAttacking(true);

        prepareDeclareBlockers();

        // Ixalli's Keeper should NOT be able to block two attackers
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(bearIdx, 0),
                new BlockerAssignment(bearIdx, 1)
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("too many times");
    }

    @Test
    @DisplayName("Spike-Tailed Ceratops can still block normally (one attacker)")
    void canBlockOneAttackerNormally() {
        SpikeTailedCeratops card = new SpikeTailedCeratops();
        Permanent ceratopsPerm = harness.addToBattlefieldAndReturn(player2, card);
        ceratopsPerm.setSummoningSick(false);

        int ceratopsIdx = gd.playerBattlefields.get(player2.getId()).indexOf(ceratopsPerm);

        IxallisKeeper atk = new IxallisKeeper();
        Permanent atkPerm = harness.addToBattlefieldAndReturn(player1, atk);
        atkPerm.setSummoningSick(false);
        atkPerm.setAttacking(true);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(ceratopsIdx, 0)
        ));

        assertThat(ceratopsPerm.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Spike-Tailed Ceratops (4/4) kills two 2/2 attackers and dies")
    void combatDamageWithMultiBlock() {
        SpikeTailedCeratops card = new SpikeTailedCeratops();
        Permanent ceratopsPerm = harness.addToBattlefieldAndReturn(player2, card);
        ceratopsPerm.setSummoningSick(false);
        ceratopsPerm.setBlocking(true);
        ceratopsPerm.addBlockingTarget(0);
        ceratopsPerm.addBlockingTarget(1);

        IxallisKeeper atk1 = new IxallisKeeper();
        Permanent atkPerm1 = harness.addToBattlefieldAndReturn(player1, atk1);
        atkPerm1.setSummoningSick(false);
        atkPerm1.setAttacking(true);

        IxallisKeeper atk2 = new IxallisKeeper();
        Permanent atkPerm2 = harness.addToBattlefieldAndReturn(player1, atk2);
        atkPerm2.setSummoningSick(false);
        atkPerm2.setAttacking(true);

        resolveCombat();

        // CR 510.1d: The blocker divides its damage among the creatures it blocks.
        // 2 to each 2/2 kills both.
        harness.handleCombatDamageAssigned(player2,
                gd.playerBattlefields.get(player2.getId()).indexOf(ceratopsPerm),
                java.util.Map.of(atkPerm1.getId(), 2, atkPerm2.getId(), 2));

        // 4/4 deals 4 damage: kills first 2/2, 2 remaining kills second 2/2
        // Both attackers deal 2 damage to Ceratops, killing it.
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player2, "Spike-Tailed Ceratops");
    }
}
