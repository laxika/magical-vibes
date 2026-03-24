package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.GrantAdditionalBlockEffect;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SpikeTailedCeratopsTest extends BaseCardTest {

    // ===== Effect structure =====

    @Test
    @DisplayName("Spike-Tailed Ceratops has GrantAdditionalBlockEffect(1) on STATIC slot")
    void hasCorrectEffect() {
        SpikeTailedCeratops card = new SpikeTailedCeratops();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(GrantAdditionalBlockEffect.class);
        GrantAdditionalBlockEffect effect = (GrantAdditionalBlockEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(effect.additionalBlocks()).isEqualTo(1);
    }

    // ===== Blocking: Ceratops can block two attackers =====

    @Test
    @DisplayName("Spike-Tailed Ceratops can block two attackers")
    void canBlockTwoAttackers() {
        SpikeTailedCeratops card = new SpikeTailedCeratops();
        Permanent ceratopsPerm = new Permanent(card);
        ceratopsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(ceratopsPerm);
        int ceratopsIdx = gd.playerBattlefields.get(player2.getId()).indexOf(ceratopsPerm);

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

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(ceratopsIdx, 0),
                new BlockerAssignment(ceratopsIdx, 1)
        ));

        assertThat(ceratopsPerm.isBlocking()).isTrue();
        assertThat(ceratopsPerm.getBlockingTargets()).containsExactlyInAnyOrder(0, 1);
    }

    // ===== Cannot block three attackers =====

    @Test
    @DisplayName("Spike-Tailed Ceratops cannot block three attackers")
    void cannotBlockThreeAttackers() {
        SpikeTailedCeratops card = new SpikeTailedCeratops();
        Permanent ceratopsPerm = new Permanent(card);
        ceratopsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(ceratopsPerm);
        int ceratopsIdx = gd.playerBattlefields.get(player2.getId()).indexOf(ceratopsPerm);

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

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(ceratopsIdx, 0),
                new BlockerAssignment(ceratopsIdx, 1),
                new BlockerAssignment(ceratopsIdx, 2)
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("too many times");
    }

    // ===== Self-only: does not grant additional blocks to other creatures =====

    @Test
    @DisplayName("Spike-Tailed Ceratops does not grant additional blocks to other creatures")
    void doesNotGrantAdditionalBlocksToOthers() {
        SpikeTailedCeratops ceratops = new SpikeTailedCeratops();
        Permanent ceratopsPerm = new Permanent(ceratops);
        ceratopsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(ceratopsPerm);

        GrizzlyBears blocker = new GrizzlyBears();
        Permanent blockerPerm = new Permanent(blocker);
        blockerPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blockerPerm);
        int bearIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);

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

        // Grizzly Bears should NOT be able to block two attackers
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(bearIdx, 0),
                new BlockerAssignment(bearIdx, 1)
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("too many times");
    }

    // ===== Normal blocking still works =====

    @Test
    @DisplayName("Spike-Tailed Ceratops can still block normally (one attacker)")
    void canBlockOneAttackerNormally() {
        SpikeTailedCeratops card = new SpikeTailedCeratops();
        Permanent ceratopsPerm = new Permanent(card);
        ceratopsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(ceratopsPerm);
        int ceratopsIdx = gd.playerBattlefields.get(player2.getId()).indexOf(ceratopsPerm);

        GrizzlyBears atk = new GrizzlyBears();
        Permanent atkPerm = new Permanent(atk);
        atkPerm.setSummoningSick(false);
        atkPerm.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(atkPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(ceratopsIdx, 0)
        ));

        assertThat(ceratopsPerm.isBlocking()).isTrue();
    }

    // ===== Combat damage with multi-block =====

    @Test
    @DisplayName("Spike-Tailed Ceratops (4/4) kills two 2/2 attackers and dies")
    void combatDamageWithMultiBlock() {
        SpikeTailedCeratops card = new SpikeTailedCeratops();
        Permanent ceratopsPerm = new Permanent(card);
        ceratopsPerm.setSummoningSick(false);
        ceratopsPerm.setBlocking(true);
        ceratopsPerm.addBlockingTarget(0);
        ceratopsPerm.addBlockingTarget(1);
        gd.playerBattlefields.get(player2.getId()).add(ceratopsPerm);

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

        harness.passBothPriorities();

        // 4/4 deals 4 damage: kills first 2/2, 2 remaining kills second 2/2
        // Both 2/2 deal 2+2=4 damage to ceratops — ceratops dies
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Spike-Tailed Ceratops"));
    }
}
