package com.github.laxika.magicalvibes;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.r.Rhox;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CombatDamageAssignmentTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;
    private GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gd = harness.getGameData();
        harness.skipMulligan();
        harness.clearMessages();
    }

    @Test
    @DisplayName("Two blockers: player distributes damage freely")
    void twoBlockersPlayerDistributesDamage() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.addToBattlefield(player2, new LlanowarElves());

        Permanent attacker = gd.playerBattlefields.get(player1.getId()).getFirst();
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        List<Permanent> defBf = gd.playerBattlefields.get(player2.getId());
        Permanent blocker1 = defBf.get(0);
        Permanent blocker2 = defBf.get(1);
        blocker1.setBlocking(true);
        blocker1.addBlockingTarget(0);
        blocker2.setBlocking(true);
        blocker2.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        // Advance from DECLARE_BLOCKERS → COMBAT_DAMAGE (paused for assignment)
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.COMBAT_DAMAGE_ASSIGNMENT);

        // Assign 1 damage to each blocker (kills both 1/1s)
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker1.getId(), 1,
                blocker2.getId(), 1
        ));

        // Both blockers should be dead
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Llanowar Elves"));
        // Player took no damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Two blockers: player can assign all damage to one blocker")
    void twoBlockersAllDamageToOne() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.addToBattlefield(player2, new LlanowarElves());

        Permanent attacker = gd.playerBattlefields.get(player1.getId()).getFirst();
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        List<Permanent> defBf = gd.playerBattlefields.get(player2.getId());
        Permanent blocker1 = defBf.get(0);
        Permanent blocker2 = defBf.get(1);
        blocker1.setBlocking(true);
        blocker1.addBlockingTarget(0);
        blocker2.setBlocking(true);
        blocker2.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // Assign all 2 damage to the first blocker
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker1.getId(), 2
        ));

        // First blocker should be dead
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(p -> p.getId().equals(blocker1.getId())).isEmpty();
        // Second blocker should survive
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(p -> p.getId().equals(blocker2.getId())).hasSize(1);
        // Player took no damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Trample creature: must assign lethal to blocker, excess to player")
    void trampleCreatureAssignsExcessToPlayer() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new AvatarOfMight());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent attacker = gd.playerBattlefields.get(player1.getId()).getFirst();
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        Permanent blocker = gd.playerBattlefields.get(player2.getId()).getFirst();
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.COMBAT_DAMAGE_ASSIGNMENT);

        // Assign lethal (2) to blocker, remaining 6 to defending player
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2,
                player2.getId(), 6
        ));

        // Blocker should be dead
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        // Player took 6 trample damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Trample creature: player can over-assign to blocker")
    void trampleCreatureCanOverAssignToBlocker() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new AvatarOfMight());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent attacker = gd.playerBattlefields.get(player1.getId()).getFirst();
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        Permanent blocker = gd.playerBattlefields.get(player2.getId()).getFirst();
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // Over-assign to blocker: 5 to blocker (only 2 needed), 3 to player
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 5,
                player2.getId(), 3
        ));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Single blocker without trample: auto-resolved without assignment prompt")
    void singleBlockerNoTrampleAutoResolved() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());

        Permanent attacker = gd.playerBattlefields.get(player1.getId()).getFirst();
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        Permanent blocker = gd.playerBattlefields.get(player2.getId()).getFirst();
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        // Should auto-resolve without prompting
        harness.passBothPriorities();

        // Blocker should be dead (2 damage kills a 1/1)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Llanowar Elves"));
        // Player took no damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Unblocked creature: auto-resolved without assignment prompt")
    void unblockedCreatureAutoResolved() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent attacker = gd.playerBattlefields.get(player1.getId()).getFirst();
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // Player took 2 damage from unblocked creature
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Rhox blocked: can assign all damage to defending player via assign-as-unblocked")
    void rhoxBlockedAssignAllToPlayer() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new Rhox());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent rhox = gd.playerBattlefields.get(player1.getId()).getFirst();
        rhox.setSummoningSick(false);
        rhox.setAttacking(true);

        Permanent blocker = gd.playerBattlefields.get(player2.getId()).getFirst();
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.COMBAT_DAMAGE_ASSIGNMENT);

        // Assign all damage to defending player (using assign-as-though-unblocked)
        harness.handleCombatDamageAssigned(player1, 0, Map.of(player2.getId(), 5));

        // Blocker survived (no damage assigned to it)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        // Player took all 5 damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Rhox blocked: can assign all damage to blocker instead")
    void rhoxBlockedAssignAllToBlocker() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new Rhox());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent rhox = gd.playerBattlefields.get(player1.getId()).getFirst();
        rhox.setSummoningSick(false);
        rhox.setAttacking(true);

        Permanent blocker = gd.playerBattlefields.get(player2.getId()).getFirst();
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // Assign all damage to blocker (declines to use assign-as-unblocked ability)
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 5));

        // Blocker should be dead (5 damage > 2 toughness)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        // Player took no damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
