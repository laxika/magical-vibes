package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.d.DarksteelAxe;
import com.github.laxika.magicalvibes.cards.e.EchoCirclet;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KembasLegion.class, DarksteelAxe.class, EchoCirclet.class, GrizzlyBears.class})
class KembasLegionTest extends BaseCardTest {

    @Test
    @DisplayName("Vigilance leaves Kemba's Legion untapped when it attacks")
    void vigilanceLeavesLegionUntapped() {
        Permanent legion = addCreatureReady(player1, new KembasLegion());
        declareAttackers(List.of(0));
        assertThat(legion.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Kemba's Legion can block one creature without Equipment")
    void canBlockOneWithoutEquipment() {
        Permanent legion = harness.addToBattlefieldAndReturn(player2, new KembasLegion());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        prepareDeclareBlockers();
        assertThatCode(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0)))).doesNotThrowAnyException();
        assertThat(legion.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("With no equipment attached, Kemba's Legion can only block one creature")
    void canOnlyBlockOneWithNoEquipment() {
        Permanent legionPerm = addCreatureReady(player2, new KembasLegion());

        for (int i = 0; i < 2; i++) {
            Permanent atkPerm = addCreatureReady(player1, new GrizzlyBears());
            atkPerm.setAttacking(true);
        }

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(legionPerm);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(blockerIdx, 0),
                new BlockerAssignment(blockerIdx, 1)
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("too many times");
    }

    @Test
    @DisplayName("With one equipment attached, Kemba's Legion can block two creatures")
    void canBlockTwoWithOneEquipment() {
        Permanent legionPerm = addCreatureReady(player2, new KembasLegion());

        // Darksteel Axe is a simple equipment with no additional block effect
        Permanent equipPerm = addCreatureReady(player2, new DarksteelAxe());
        equipPerm.setAttachedTo(legionPerm.getId());

        for (int i = 0; i < 2; i++) {
            Permanent atkPerm = addCreatureReady(player1, new GrizzlyBears());
            atkPerm.setAttacking(true);
        }

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(legionPerm);

        // Blocking 2 creatures should be legal with 1 equipment (max = 1 base + 1 per equipment = 2)
        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(blockerIdx, 0),
                new BlockerAssignment(blockerIdx, 1)
        ))).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("With two equipment attached, Kemba's Legion can block three creatures")
    void canBlockThreeWithTwoEquipment() {
        Permanent legionPerm = addCreatureReady(player2, new KembasLegion());

        for (int i = 0; i < 2; i++) {
            Permanent equipPerm = addCreatureReady(player2, new DarksteelAxe());
            equipPerm.setAttachedTo(legionPerm.getId());
        }

        for (int i = 0; i < 3; i++) {
            Permanent atkPerm = addCreatureReady(player1, new GrizzlyBears());
            atkPerm.setAttacking(true);
        }

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(legionPerm);

        // Blocking 3 creatures should be legal with 2 equipment (max = 1 base + 2 per equipment = 3)
        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(blockerIdx, 0),
                new BlockerAssignment(blockerIdx, 1),
                new BlockerAssignment(blockerIdx, 2)
        ))).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("With one equipment, Kemba's Legion cannot block three creatures")
    void cannotExceedMaxBlocksWithOneEquipment() {
        Permanent legionPerm = addCreatureReady(player2, new KembasLegion());

        // Darksteel Axe: no additional block effect, so max = 1 base + 1 per-equipment = 2
        Permanent equipPerm = addCreatureReady(player2, new DarksteelAxe());
        equipPerm.setAttachedTo(legionPerm.getId());

        for (int i = 0; i < 3; i++) {
            Permanent atkPerm = addCreatureReady(player1, new GrizzlyBears());
            atkPerm.setAttacking(true);
        }

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(legionPerm);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(blockerIdx, 0),
                new BlockerAssignment(blockerIdx, 1),
                new BlockerAssignment(blockerIdx, 2)
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("too many times");
    }

    @Test
    @DisplayName("Unattached equipment on battlefield does not grant additional blocks")
    void unattachedEquipmentDoesNotCount() {
        Permanent legionPerm = addCreatureReady(player2, new KembasLegion());

        // Equipment on battlefield but NOT attached to Kemba's Legion
        Permanent equipPerm = addCreatureReady(player2, new DarksteelAxe());

        for (int i = 0; i < 2; i++) {
            Permanent atkPerm = addCreatureReady(player1, new GrizzlyBears());
            atkPerm.setAttacking(true);
        }

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(legionPerm);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(blockerIdx, 0),
                new BlockerAssignment(blockerIdx, 1)
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("too many times");
    }

    @Test
    @DisplayName("Kemba's Legion effect does not grant additional blocks to other creatures")
    void effectDoesNotApplyToOtherCreatures() {
        Permanent legionPerm = addCreatureReady(player2, new KembasLegion());

        // Attach equipment to Kemba's Legion
        Permanent equipPerm = addCreatureReady(player2, new DarksteelAxe());
        equipPerm.setAttachedTo(legionPerm.getId());

        // Another creature without equipment
        Permanent otherPerm = addCreatureReady(player2, new GrizzlyBears());

        for (int i = 0; i < 2; i++) {
            Permanent atkPerm = addCreatureReady(player1, new GrizzlyBears());
            atkPerm.setAttacking(true);
        }

        prepareDeclareBlockers();

        int otherIdx = gd.playerBattlefields.get(player2.getId()).indexOf(otherPerm);

        // Other creature should not be able to block two
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(otherIdx, 0),
                new BlockerAssignment(otherIdx, 1)
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("too many times");
    }

    @Test
    @DisplayName("Opponent-controlled attached Equipment still grants an additional block")
    void countsEquipmentControlledByOpponent() {
        Permanent legion = harness.addToBattlefieldAndReturn(player2, new KembasLegion());
        Permanent axe = harness.addToBattlefieldAndReturn(player1, new DarksteelAxe());
        axe.setAttachedTo(legion.getId());

        for (int i = 0; i < 2; i++) {
            Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
            attacker.setAttacking(true);
        }

        prepareDeclareBlockers();

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 1),
                new BlockerAssignment(0, 2)
        ))).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Kemba's Legion with Echo Circlet gets +1 from equipment count AND +1 from Echo Circlet's own effect")
    void stacksWithEchoCircletEffect() {
        Permanent legionPerm = addCreatureReady(player2, new KembasLegion());

        // Echo Circlet has both GrantAdditionalBlockEffect(1) AND counts as equipment
        Permanent equipPerm = addCreatureReady(player2, new EchoCirclet());
        equipPerm.setAttachedTo(legionPerm.getId());

        // With one Echo Circlet: base 1 + 1 (equipment count) + 1 (Echo Circlet's own effect) = 3
        for (int i = 0; i < 3; i++) {
            Permanent atkPerm = addCreatureReady(player1, new GrizzlyBears());
            atkPerm.setAttacking(true);
        }

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(legionPerm);

        // Kemba's Legion (4/6) blocks three 2/2 attackers → takes 6 damage → dies
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(blockerIdx, 0),
                new BlockerAssignment(blockerIdx, 1),
                new BlockerAssignment(blockerIdx, 2)
        ));

        // Creature dies in combat so blocking flag persists on the permanent object
        assertThat(legionPerm.isBlocking()).isTrue();
        assertThat(legionPerm.getBlockingTargets()).containsExactlyInAnyOrder(0, 1, 2);
    }
}
