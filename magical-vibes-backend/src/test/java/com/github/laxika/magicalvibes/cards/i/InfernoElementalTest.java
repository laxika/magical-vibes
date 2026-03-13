package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InfernoElementalTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has DealDamageToTargetCreatureEffect(3) on both ON_BLOCK and ON_BECOMES_BLOCKED")
    void hasCorrectEffects() {
        InfernoElemental card = new InfernoElemental();

        assertThat(card.getEffects(EffectSlot.ON_BLOCK)).singleElement()
                .isInstanceOf(DealDamageToTargetCreatureEffect.class);
        assertThat(card.getEffects(EffectSlot.ON_BECOMES_BLOCKED)).singleElement()
                .isInstanceOf(DealDamageToTargetCreatureEffect.class);
    }

    // ===== When Inferno Elemental blocks =====

    @Test
    @DisplayName("Blocking creates a trigger that deals 3 damage to the attacker")
    void blockingDeals3DamageToAttacker() {
        Permanent elemental = addReadyElemental(player2);
        Permanent attacker = addReadyBears(player1);
        attacker.setAttacking(true);

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Inferno Elemental");
        assertThat(entry.getTargetPermanentId()).isEqualTo(attacker.getId());

        harness.passBothPriorities();

        // Attacker (2/2) takes 3 damage and dies
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    // ===== When Inferno Elemental becomes blocked =====

    @Test
    @DisplayName("Becoming blocked creates a trigger that deals 3 damage to the blocker")
    void becomingBlockedDeals3DamageToBlocker() {
        Permanent elemental = addReadyElemental(player1);
        elemental.setAttacking(true);
        Permanent blocker = addReadyBears(player2);

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getTargetPermanentId()).isEqualTo(blocker.getId());
        assertThat(entry.getSourcePermanentId()).isEqualTo(elemental.getId());

        harness.passBothPriorities();

        // Blocker (2/2) takes 3 damage and dies
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Becoming blocked by multiple creatures creates one trigger per blocker")
    void becomingBlockedByMultipleCreaturesCreatesMultipleTriggers() {
        Permanent elemental = addReadyElemental(player1);
        elemental.setAttacking(true);
        addReadyBears(player2);
        addReadyBears(player2);

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));

        long triggerCount = gd.stack.stream()
                .filter(e -> e.getCard().getName().equals("Inferno Elemental"))
                .count();
        assertThat(triggerCount).isEqualTo(2);

        harness.passBothPriorities();
        harness.passBothPriorities();

        // Both blockers (2/2) take 3 damage and die
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .filteredOn(c -> c.getName().equals("Grizzly Bears"))
                .hasSize(2);
    }

    // ===== Trigger is non-targeting =====

    @Test
    @DisplayName("Block trigger is non-targeting (cannot be fizzled by shroud/hexproof)")
    void blockTriggerIsNonTargeting() {
        Permanent elemental = addReadyElemental(player1);
        elemental.setAttacking(true);
        addReadyBears(player2);

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.isNonTargeting()).isTrue();
    }

    // ===== Helpers =====

    private Permanent addReadyElemental(Player player) {
        Permanent perm = new Permanent(new InfernoElemental());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyBears(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void setupDeclareBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);
    }
}
