package com.github.laxika.magicalvibes.cards.a;

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

class AshmouthHoundTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has DealDamageToTargetCreatureEffect(1) on both ON_BLOCK and ON_BECOMES_BLOCKED")
    void hasCorrectEffects() {
        AshmouthHound card = new AshmouthHound();

        assertThat(card.getEffects(EffectSlot.ON_BLOCK)).singleElement()
                .isInstanceOf(DealDamageToTargetCreatureEffect.class);
        assertThat(card.getEffects(EffectSlot.ON_BECOMES_BLOCKED)).singleElement()
                .isInstanceOf(DealDamageToTargetCreatureEffect.class);
    }

    // ===== When Ashmouth Hound blocks =====

    @Test
    @DisplayName("Blocking creates a trigger that deals 1 damage to the attacker")
    void blockingDeals1DamageToAttacker() {
        Permanent hound = addReadyHound(player2);
        Permanent attacker = addReadyBears(player1);
        attacker.setAttacking(true);

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Ashmouth Hound");
        assertThat(entry.getTargetPermanentId()).isEqualTo(attacker.getId());

        harness.passBothPriorities();

        // Attacker (2/2) takes 1 damage but survives
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        Permanent damagedAttacker = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        assertThat(damagedAttacker.getMarkedDamage()).isEqualTo(1);
    }

    // ===== When Ashmouth Hound becomes blocked =====

    @Test
    @DisplayName("Becoming blocked creates a trigger that deals 1 damage to the blocker")
    void becomingBlockedDeals1DamageToBlocker() {
        Permanent hound = addReadyHound(player1);
        hound.setAttacking(true);
        Permanent blocker = addReadyBears(player2);

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getTargetPermanentId()).isEqualTo(blocker.getId());
        assertThat(entry.getSourcePermanentId()).isEqualTo(hound.getId());

        harness.passBothPriorities();

        // Blocker (2/2) takes 1 damage but survives
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        Permanent damagedBlocker = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        assertThat(damagedBlocker.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Becoming blocked by multiple creatures creates one trigger per blocker")
    void becomingBlockedByMultipleCreaturesCreatesMultipleTriggers() {
        Permanent hound = addReadyHound(player1);
        hound.setAttacking(true);
        addReadyBears(player2);
        addReadyBears(player2);

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));

        long triggerCount = gd.stack.stream()
                .filter(e -> e.getCard().getName().equals("Ashmouth Hound"))
                .count();
        assertThat(triggerCount).isEqualTo(2);

        harness.passBothPriorities();
        harness.passBothPriorities();

        // Both blockers (2/2) take 1 damage but survive
        List<Permanent> bears = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .toList();
        assertThat(bears).hasSize(2);
        assertThat(bears).allMatch(p -> p.getMarkedDamage() == 1);
    }

    // ===== Trigger is non-targeting =====

    @Test
    @DisplayName("Block trigger is non-targeting (cannot be fizzled by shroud/hexproof)")
    void blockTriggerIsNonTargeting() {
        Permanent hound = addReadyHound(player1);
        hound.setAttacking(true);
        addReadyBears(player2);

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.isNonTargeting()).isTrue();
    }

    // ===== Helpers =====

    private Permanent addReadyHound(Player player) {
        Permanent perm = new Permanent(new AshmouthHound());
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
