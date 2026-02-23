package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BlackcleaveGoblinTest extends BaseCardTest {

    // ===== Casting =====

    @Test
    @DisplayName("Blackcleave Goblin resolves onto the battlefield")
    void resolvesOntoBattlefield() {
        harness.setHand(player1, List.of(new BlackcleaveGoblin()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Blackcleave Goblin");
    }

    // ===== Infect: combat damage deals poison to player =====

    @Test
    @DisplayName("Unblocked Blackcleave Goblin deals poison counters instead of life loss")
    void dealsPoisonCountersWhenUnblocked() {
        harness.setLife(player2, 20);

        Permanent atkPerm = new Permanent(new BlackcleaveGoblin());
        atkPerm.setSummoningSick(false);
        atkPerm.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(atkPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Life should remain unchanged
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        // Poison counters should equal power (2)
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(2);
    }

    // ===== Infect: combat damage deals -1/-1 counters to creatures =====

    @Test
    @DisplayName("Blocked Blackcleave Goblin deals -1/-1 counters to blocker instead of regular damage")
    void dealsMinusCountersToBlocker() {
        // Grizzly Bears is 2/2
        Permanent blockerPerm = new Permanent(new GrizzlyBears());
        blockerPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blockerPerm);

        // Blackcleave Goblin is 2/1
        Permanent atkPerm = new Permanent(new BlackcleaveGoblin());
        atkPerm.setSummoningSick(false);
        atkPerm.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(atkPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(atkPerm);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));
        harness.passBothPriorities();

        // Blackcleave Goblin (2/1) dies to Grizzly Bears (2/2)
        harness.assertNotOnBattlefield(player1, "Blackcleave Goblin");
        harness.assertInGraveyard(player1, "Blackcleave Goblin");

        // Grizzly Bears should have 2 -1/-1 counters (from 2 infect damage), making it 0/0 → dies
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");

        // No poison counters — damage went to a creature
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(0);
    }

    // ===== 10 poison counters loses the game =====

    @Test
    @DisplayName("Player with 10 or more poison counters loses the game")
    void tenPoisonCountersLosesGame() {
        harness.setLife(player2, 20);
        // Give player2 8 poison counters already
        gd.playerPoisonCounters.put(player2.getId(), 8);

        Permanent atkPerm = new Permanent(new BlackcleaveGoblin());
        atkPerm.setSummoningSick(false);
        atkPerm.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(atkPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // 8 + 2 = 10 poison → player2 loses
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(10);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        // Life should still be 20
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    // ===== Mixed combat: infect + non-infect =====

    @Test
    @DisplayName("Mixed attackers: infect deals poison, non-infect deals life loss")
    void mixedAttackersInfectAndNonInfect() {
        harness.setLife(player2, 20);

        // Blackcleave Goblin (infect 2/1)
        Permanent infectAttacker = new Permanent(new BlackcleaveGoblin());
        infectAttacker.setSummoningSick(false);
        infectAttacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(infectAttacker);

        // Grizzly Bears (non-infect 2/2)
        Permanent normalAttacker = new Permanent(new GrizzlyBears());
        normalAttacker.setSummoningSick(false);
        normalAttacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(normalAttacker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Grizzly Bears deals 2 regular damage → life goes from 20 to 18
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        // Blackcleave Goblin deals 2 poison counters
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(2);
    }
}
