package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BraveTheSands.class, GrizzlyBears.class})
class BraveTheSandsTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures you control gain vigilance")
    void ownCreaturesGainVigilance() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new BraveTheSands());

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Vigilance is removed when Brave the Sands leaves the battlefield")
    void vigilanceRemovedWhenSourceLeaves() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new BraveTheSands());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(permanent -> permanent.getCard().getName().equals("Brave the Sands"));

        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("A creature you control can block two attackers")
    void ownCreatureCanBlockTwoAttackers() {
        harness.addToBattlefield(player2, new BraveTheSands());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        addAttackers(2);
        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(blockerIndex, 0),
                new BlockerAssignment(blockerIndex, 1)
        ));

        assertThat(blocker.isBlocking()).isTrue();
        assertThat(blocker.getBlockingTargets()).containsExactlyInAnyOrder(0, 1);
    }

    @Test
    @DisplayName("Brave the Sands does not grant additional blocks to an opponent's creature")
    void opponentCreatureCannotBlockTwoAttackers() {
        harness.addToBattlefield(player1, new BraveTheSands());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        addAttackers(2);
        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(blockerIndex, 1),
                new BlockerAssignment(blockerIndex, 2)
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("too many times");
    }

    @Test
    @DisplayName("A creature entering after Brave the Sands attacks without tapping")
    void laterCreatureAttacksWithoutTapping() {
        harness.addToBattlefield(player1, new BraveTheSands());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(1));

        assertThat(attacker.isAttacking()).isTrue();
        assertThat(attacker.isTapped()).isFalse();
    }

    @Test
    @DisplayName("One Brave the Sands does not allow a creature to block three attackers")
    void oneCopyDoesNotAllowThreeBlocks() {
        harness.addToBattlefield(player2, new BraveTheSands());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        addAttackers(3);
        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(blockerIndex, 0),
                new BlockerAssignment(blockerIndex, 1),
                new BlockerAssignment(blockerIndex, 2)
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("too many times");
    }

    @Test
    @DisplayName("Two copies allow one creature to block three attackers")
    void additionalBlocksFromMultipleCopiesStack() {
        harness.addToBattlefield(player2, new BraveTheSands());
        harness.addToBattlefield(player2, new BraveTheSands());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        addAttackers(3);
        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(blockerIndex, 0),
                new BlockerAssignment(blockerIndex, 1),
                new BlockerAssignment(blockerIndex, 2)
        ));

        assertThat(blocker.isBlocking()).isTrue();
        assertThat(blocker.getBlockingTargets()).containsExactlyInAnyOrder(0, 1, 2);
    }

    @Test
    @DisplayName("The additional block permission ends when Brave the Sands leaves")
    void additionalBlockPermissionEndsWhenSourceLeaves() {
        Permanent source = harness.addToBattlefieldAndReturn(player2, new BraveTheSands());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        addAttackers(2);
        gd.playerBattlefields.get(player2.getId()).remove(source);
        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(blockerIndex, 0),
                new BlockerAssignment(blockerIndex, 1)
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("too many times");
    }

    private void addAttackers(int count) {
        for (int i = 0; i < count; i++) {
            Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
            attacker.setAttacking(true);
        }
    }
}
