package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetCreatureAndGainLifeEqualToToughnessEffect;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EngulfingSlagwurmTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has destroy-and-gain-life effect on both ON_BLOCK and ON_BECOMES_BLOCKED")
    void hasCorrectEffects() {
        EngulfingSlagwurm card = new EngulfingSlagwurm();

        assertThat(card.getEffects(EffectSlot.ON_BLOCK)).singleElement()
                .isInstanceOf(DestroyTargetCreatureAndGainLifeEqualToToughnessEffect.class);
        assertThat(card.getEffects(EffectSlot.ON_BECOMES_BLOCKED)).singleElement()
                .isInstanceOf(DestroyTargetCreatureAndGainLifeEqualToToughnessEffect.class);
    }

    // ===== When Slagwurm blocks =====

    @Test
    @DisplayName("Blocking creates a trigger that destroys the attacker and gains life equal to its toughness")
    void blockingDestroysAttackerAndGainsLife() {
        harness.setLife(player2, 20);
        Permanent slagwurm = addReadySlagwurm(player2);
        Permanent attacker = addReadyBears(player1);
        attacker.setAttacking(true);

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Engulfing Slagwurm");
        assertThat(entry.getTargetPermanentId()).isEqualTo(attacker.getId());

        harness.passBothPriorities();

        // Attacker destroyed
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));

        // Slagwurm still alive
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Engulfing Slagwurm"));

        // Controller gained life equal to attacker's toughness (2)
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(22);
    }

    // ===== When Slagwurm becomes blocked =====

    @Test
    @DisplayName("Becoming blocked creates a trigger that destroys the blocker and gains life equal to its toughness")
    void becomingBlockedDestroysBlockerAndGainsLife() {
        harness.setLife(player1, 20);
        Permanent slagwurm = addReadySlagwurm(player1);
        slagwurm.setAttacking(true);
        Permanent blocker = addReadyBears(player2);

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getTargetPermanentId()).isEqualTo(blocker.getId());
        assertThat(entry.getSourcePermanentId()).isEqualTo(slagwurm.getId());

        harness.passBothPriorities();

        // Blocker destroyed
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));

        // Controller gained life equal to blocker's toughness (2)
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Becoming blocked by multiple creatures creates one trigger per blocker")
    void becomingBlockedByMultipleCreaturesCreatesMultipleTriggers() {
        harness.setLife(player1, 20);
        Permanent slagwurm = addReadySlagwurm(player1);
        slagwurm.setAttacking(true);
        addReadyBears(player2);
        addReadyBears(player2);

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));

        long triggerCount = gd.stack.stream()
                .filter(e -> e.getCard().getName().equals("Engulfing Slagwurm"))
                .count();
        assertThat(triggerCount).isEqualTo(2);

        harness.passBothPriorities();
        harness.passBothPriorities();

        // Both blockers destroyed
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .filteredOn(c -> c.getName().equals("Grizzly Bears"))
                .hasSize(2);

        // Gained 2 life per blocker (toughness 2 each)
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
    }

    // ===== Life gain still happens if creature is indestructible =====

    @Test
    @DisplayName("Life gain occurs even if target creature is indestructible")
    void lifeGainOccursEvenIfTargetIsIndestructible() {
        harness.setLife(player1, 20);
        Permanent slagwurm = addReadySlagwurm(player1);
        slagwurm.setAttacking(true);

        Permanent blocker = addReadyBears(player2);
        blocker.getGrantedKeywords().add(com.github.laxika.magicalvibes.model.Keyword.INDESTRUCTIBLE);

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        // Blocker survives (indestructible)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));

        // Controller still gains life
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    // ===== Trigger fizzles if target creature is gone =====

    @Test
    @DisplayName("Trigger does nothing if target creature is removed before resolution")
    void triggerFizzlesIfTargetGone() {
        harness.setLife(player1, 20);
        Permanent slagwurm = addReadySlagwurm(player1);
        slagwurm.setAttacking(true);
        addReadyBears(player2);

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        // Remove blocker before trigger resolves
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        // No life gain (target gone, can't determine toughness)
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    // ===== Helpers =====

    private Permanent addReadySlagwurm(Player player) {
        Permanent perm = new Permanent(new EngulfingSlagwurm());
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
