package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.GrantAdditionalBlockPerEquipmentEffect;
import com.github.laxika.magicalvibes.cards.d.DarksteelAxe;
import com.github.laxika.magicalvibes.cards.e.EchoCirclet;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KembasLegionTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Kemba's Legion has GrantAdditionalBlockPerEquipmentEffect")
    void hasCorrectEffect() {
        KembasLegion card = new KembasLegion();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(GrantAdditionalBlockPerEquipmentEffect.class);
    }

    // ===== Blocking with no equipment =====

    @Test
    @DisplayName("With no equipment attached, Kemba's Legion can only block one creature")
    void canOnlyBlockOneWithNoEquipment() {
        Permanent legionPerm = new Permanent(new KembasLegion());
        legionPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(legionPerm);

        for (int i = 0; i < 2; i++) {
            Permanent atkPerm = new Permanent(new GrizzlyBears());
            atkPerm.setSummoningSick(false);
            atkPerm.setAttacking(true);
            gd.playerBattlefields.get(player1.getId()).add(atkPerm);
        }

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(legionPerm);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(blockerIdx, 0),
                new BlockerAssignment(blockerIdx, 1)
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("too many times");
    }

    // ===== Blocking with one equipment =====

    @Test
    @DisplayName("With one equipment attached, Kemba's Legion can block two creatures")
    void canBlockTwoWithOneEquipment() {
        Permanent legionPerm = new Permanent(new KembasLegion());
        legionPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(legionPerm);

        // Darksteel Axe is a simple equipment with no additional block effect
        Permanent equipPerm = new Permanent(new DarksteelAxe());
        equipPerm.setSummoningSick(false);
        equipPerm.setAttachedTo(legionPerm.getId());
        gd.playerBattlefields.get(player2.getId()).add(equipPerm);

        for (int i = 0; i < 2; i++) {
            Permanent atkPerm = new Permanent(new GrizzlyBears());
            atkPerm.setSummoningSick(false);
            atkPerm.setAttacking(true);
            gd.playerBattlefields.get(player1.getId()).add(atkPerm);
        }

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(legionPerm);

        // Blocking 2 creatures should be legal with 1 equipment (max = 1 base + 1 per equipment = 2)
        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(blockerIdx, 0),
                new BlockerAssignment(blockerIdx, 1)
        ))).doesNotThrowAnyException();
    }

    // ===== Blocking with two equipment =====

    @Test
    @DisplayName("With two equipment attached, Kemba's Legion can block three creatures")
    void canBlockThreeWithTwoEquipment() {
        Permanent legionPerm = new Permanent(new KembasLegion());
        legionPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(legionPerm);

        for (int i = 0; i < 2; i++) {
            Permanent equipPerm = new Permanent(new DarksteelAxe());
            equipPerm.setSummoningSick(false);
            equipPerm.setAttachedTo(legionPerm.getId());
            gd.playerBattlefields.get(player2.getId()).add(equipPerm);
        }

        for (int i = 0; i < 3; i++) {
            Permanent atkPerm = new Permanent(new GrizzlyBears());
            atkPerm.setSummoningSick(false);
            atkPerm.setAttacking(true);
            gd.playerBattlefields.get(player1.getId()).add(atkPerm);
        }

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(legionPerm);

        // Blocking 3 creatures should be legal with 2 equipment (max = 1 base + 2 per equipment = 3)
        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(blockerIdx, 0),
                new BlockerAssignment(blockerIdx, 1),
                new BlockerAssignment(blockerIdx, 2)
        ))).doesNotThrowAnyException();
    }

    // ===== Cannot exceed max blocks =====

    @Test
    @DisplayName("With one equipment, Kemba's Legion cannot block three creatures")
    void cannotExceedMaxBlocksWithOneEquipment() {
        Permanent legionPerm = new Permanent(new KembasLegion());
        legionPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(legionPerm);

        // Darksteel Axe: no additional block effect, so max = 1 base + 1 per-equipment = 2
        Permanent equipPerm = new Permanent(new DarksteelAxe());
        equipPerm.setSummoningSick(false);
        equipPerm.setAttachedTo(legionPerm.getId());
        gd.playerBattlefields.get(player2.getId()).add(equipPerm);

        for (int i = 0; i < 3; i++) {
            Permanent atkPerm = new Permanent(new GrizzlyBears());
            atkPerm.setSummoningSick(false);
            atkPerm.setAttacking(true);
            gd.playerBattlefields.get(player1.getId()).add(atkPerm);
        }

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(legionPerm);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(blockerIdx, 0),
                new BlockerAssignment(blockerIdx, 1),
                new BlockerAssignment(blockerIdx, 2)
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("too many times");
    }

    // ===== Unattached equipment does not count =====

    @Test
    @DisplayName("Unattached equipment on battlefield does not grant additional blocks")
    void unattachedEquipmentDoesNotCount() {
        Permanent legionPerm = new Permanent(new KembasLegion());
        legionPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(legionPerm);

        // Equipment on battlefield but NOT attached to Kemba's Legion
        Permanent equipPerm = new Permanent(new DarksteelAxe());
        equipPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(equipPerm);

        for (int i = 0; i < 2; i++) {
            Permanent atkPerm = new Permanent(new GrizzlyBears());
            atkPerm.setSummoningSick(false);
            atkPerm.setAttacking(true);
            gd.playerBattlefields.get(player1.getId()).add(atkPerm);
        }

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(legionPerm);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(blockerIdx, 0),
                new BlockerAssignment(blockerIdx, 1)
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("too many times");
    }

    // ===== Effect only applies to self =====

    @Test
    @DisplayName("Kemba's Legion effect does not grant additional blocks to other creatures")
    void effectDoesNotApplyToOtherCreatures() {
        Permanent legionPerm = new Permanent(new KembasLegion());
        legionPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(legionPerm);

        // Attach equipment to Kemba's Legion
        Permanent equipPerm = new Permanent(new DarksteelAxe());
        equipPerm.setSummoningSick(false);
        equipPerm.setAttachedTo(legionPerm.getId());
        gd.playerBattlefields.get(player2.getId()).add(equipPerm);

        // Another creature without equipment
        Permanent otherPerm = new Permanent(new GrizzlyBears());
        otherPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(otherPerm);

        for (int i = 0; i < 2; i++) {
            Permanent atkPerm = new Permanent(new GrizzlyBears());
            atkPerm.setSummoningSick(false);
            atkPerm.setAttacking(true);
            gd.playerBattlefields.get(player1.getId()).add(atkPerm);
        }

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

        int otherIdx = gd.playerBattlefields.get(player2.getId()).indexOf(otherPerm);

        // Other creature should not be able to block two
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(otherIdx, 0),
                new BlockerAssignment(otherIdx, 1)
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("too many times");
    }

    // ===== Stacks with Echo Circlet's own GrantAdditionalBlockEffect =====

    @Test
    @DisplayName("Kemba's Legion with Echo Circlet gets +1 from equipment count AND +1 from Echo Circlet's own effect")
    void stacksWithEchoCircletEffect() {
        Permanent legionPerm = new Permanent(new KembasLegion());
        legionPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(legionPerm);

        // Echo Circlet has both GrantAdditionalBlockEffect(1) AND counts as equipment
        Permanent equipPerm = new Permanent(new EchoCirclet());
        equipPerm.setSummoningSick(false);
        equipPerm.setAttachedTo(legionPerm.getId());
        gd.playerBattlefields.get(player2.getId()).add(equipPerm);

        // With one Echo Circlet: base 1 + 1 (equipment count) + 1 (Echo Circlet's own effect) = 3
        for (int i = 0; i < 3; i++) {
            Permanent atkPerm = new Permanent(new GrizzlyBears());
            atkPerm.setSummoningSick(false);
            atkPerm.setAttacking(true);
            gd.playerBattlefields.get(player1.getId()).add(atkPerm);
        }

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

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
