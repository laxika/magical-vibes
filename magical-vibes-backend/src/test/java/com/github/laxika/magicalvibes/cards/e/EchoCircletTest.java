package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.EquipEffect;
import com.github.laxika.magicalvibes.model.effect.GrantAdditionalBlockEffect;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EchoCircletTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Echo Circlet has GrantAdditionalBlockEffect and equip ability")
    void hasCorrectProperties() {
        EchoCirclet card = new EchoCirclet();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst()).isInstanceOf(GrantAdditionalBlockEffect.class);
        GrantAdditionalBlockEffect effect = (GrantAdditionalBlockEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(effect.additionalBlocks()).isEqualTo(1);

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().getFirst().getEffects())
                .anyMatch(EquipEffect.class::isInstance);
    }

    // ===== Equipped creature can block two attackers =====

    @Test
    @DisplayName("Equipped creature can block two attackers")
    void equippedCreatureCanBlockTwo() {
        Permanent circletPerm = new Permanent(new EchoCirclet());
        circletPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(circletPerm);

        GrizzlyBears blocker = new GrizzlyBears();
        Permanent blockerPerm = new Permanent(blocker);
        blockerPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blockerPerm);

        // Attach circlet to blocker
        circletPerm.setAttachedTo(blockerPerm.getId());

        // Player1 has two attacking creatures
        GrizzlyBears atk1 = new GrizzlyBears();
        Permanent atkPerm1 = new Permanent(atk1);
        atkPerm1.setSummoningSick(false);
        atkPerm1.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(atkPerm1);

        GrizzlyBears atk2 = new GrizzlyBears();
        Permanent atkPerm2 = new Permanent(atk2);
        atkPerm2.setSummoningSick(false);
        atkPerm2.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(atkPerm2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(blockerIdx, 0),
                new BlockerAssignment(blockerIdx, 1)
        ));

        assertThat(blockerPerm.isBlocking()).isTrue();
        assertThat(blockerPerm.getBlockingTargets()).containsExactlyInAnyOrder(0, 1);
    }

    // ===== Unequipped creature cannot block two attackers =====

    @Test
    @DisplayName("Unequipped creature cannot block two attackers even with Echo Circlet on battlefield")
    void unequippedCreatureCannotBlockTwo() {
        // Echo Circlet on battlefield but NOT attached to any creature
        Permanent circletPerm = new Permanent(new EchoCirclet());
        circletPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(circletPerm);

        GrizzlyBears blocker = new GrizzlyBears();
        Permanent blockerPerm = new Permanent(blocker);
        blockerPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blockerPerm);

        GrizzlyBears atk1 = new GrizzlyBears();
        Permanent atkPerm1 = new Permanent(atk1);
        atkPerm1.setSummoningSick(false);
        atkPerm1.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(atkPerm1);

        GrizzlyBears atk2 = new GrizzlyBears();
        Permanent atkPerm2 = new Permanent(atk2);
        atkPerm2.setSummoningSick(false);
        atkPerm2.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(atkPerm2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(blockerIdx, 0),
                new BlockerAssignment(blockerIdx, 1)
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("too many times");
    }

    // ===== Only equipped creature gets the bonus =====

    @Test
    @DisplayName("Only the equipped creature can block two, other creature cannot")
    void onlyEquippedCreatureGetsBonus() {
        Permanent circletPerm = new Permanent(new EchoCirclet());
        circletPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(circletPerm);

        GrizzlyBears equipped = new GrizzlyBears();
        Permanent equippedPerm = new Permanent(equipped);
        equippedPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(equippedPerm);

        GrizzlyBears other = new GrizzlyBears();
        Permanent otherPerm = new Permanent(other);
        otherPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(otherPerm);

        // Attach circlet to first creature only
        circletPerm.setAttachedTo(equippedPerm.getId());

        // Three attackers
        for (int i = 0; i < 3; i++) {
            GrizzlyBears atk = new GrizzlyBears();
            Permanent atkPerm = new Permanent(atk);
            atkPerm.setSummoningSick(false);
            atkPerm.setAttacking(true);
            gd.playerBattlefields.get(player1.getId()).add(atkPerm);
        }

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

        int otherIdx = gd.playerBattlefields.get(player2.getId()).indexOf(otherPerm);

        // Other creature tries to block two — should fail
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(otherIdx, 0),
                new BlockerAssignment(otherIdx, 1)
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("too many times");
    }

    // ===== Equipped creature cannot exceed max blocks =====

    @Test
    @DisplayName("Equipped creature cannot block three attackers with one Echo Circlet")
    void equippedCreatureCannotExceedMaxBlocks() {
        Permanent circletPerm = new Permanent(new EchoCirclet());
        circletPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(circletPerm);

        GrizzlyBears blocker = new GrizzlyBears();
        Permanent blockerPerm = new Permanent(blocker);
        blockerPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blockerPerm);

        circletPerm.setAttachedTo(blockerPerm.getId());

        for (int i = 0; i < 3; i++) {
            GrizzlyBears atk = new GrizzlyBears();
            Permanent atkPerm = new Permanent(atk);
            atkPerm.setSummoningSick(false);
            atkPerm.setAttacking(true);
            gd.playerBattlefields.get(player1.getId()).add(atkPerm);
        }

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(blockerIdx, 0),
                new BlockerAssignment(blockerIdx, 1),
                new BlockerAssignment(blockerIdx, 2)
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("too many times");
    }

    // ===== Equip via activated ability =====

    @Test
    @DisplayName("Equip ability attaches Echo Circlet to target creature")
    void equipAbilityAttaches() {
        Permanent circletPerm = new Permanent(new EchoCirclet());
        circletPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(circletPerm);

        GrizzlyBears creature = new GrizzlyBears();
        Permanent creaturePerm = new Permanent(creature);
        creaturePerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(creaturePerm);

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, creaturePerm.getId());
        harness.passBothPriorities();

        assertThat(circletPerm.getAttachedTo()).isEqualTo(creaturePerm.getId());
    }
}
