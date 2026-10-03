package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AlphaTyrranax;
import com.github.laxika.magicalvibes.cards.c.CarapaceForger;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BlackcleaveGoblin.class, CarapaceForger.class, AlphaTyrranax.class})
class BlackcleaveGoblinTest extends BaseCardTest {

    @Test
    @DisplayName("Blackcleave Goblin resolves onto the battlefield")
    void resolvesOntoBattlefield() {
        harness.setHand(player1, List.of(new BlackcleaveGoblin()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Blackcleave Goblin");
    }

    @Test
    @DisplayName("Unblocked Blackcleave Goblin deals poison counters instead of life loss")
    void dealsPoisonCountersWhenUnblocked() {
        harness.setLife(player2, 20);

        Permanent atkPerm = addCreatureReady(player1, new BlackcleaveGoblin());
        atkPerm.setAttacking(true);

        resolveCombat();

        // Life should remain unchanged
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        // Poison counters should equal power (2)
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(2);
    }

    @Test
    @DisplayName("Blocked Blackcleave Goblin deals -1/-1 counters to blocker instead of regular damage")
    void dealsMinusCountersToBlocker() {
        // Carapace Forger is 2/2
        Permanent blockerPerm = addCreatureReady(player2, new CarapaceForger());

        // Blackcleave Goblin is 2/1
        Permanent atkPerm = addCreatureReady(player1, new BlackcleaveGoblin());
        atkPerm.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(atkPerm);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));
        harness.passBothPriorities();

        // Blackcleave Goblin (2/1) dies to Carapace Forger (2/2)
        harness.assertNotOnBattlefield(player1, "Blackcleave Goblin");
        harness.assertInGraveyard(player1, "Blackcleave Goblin");

        // Carapace Forger should have 2 -1/-1 counters (from 2 infect damage), making it 0/0 → dies
        harness.assertNotOnBattlefield(player2, "Carapace Forger");
        harness.assertInGraveyard(player2, "Carapace Forger");

        // No poison counters — damage went to a creature
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(0);
    }

    @Test
    @DisplayName("Player with 10 or more poison counters loses the game")
    void tenPoisonCountersLosesGame() {
        harness.setLife(player2, 20);
        // Give player2 8 poison counters already
        gd.playerPoisonCounters.put(player2.getId(), 8);

        Permanent atkPerm = addCreatureReady(player1, new BlackcleaveGoblin());
        atkPerm.setAttacking(true);

        resolveCombat();

        // 8 + 2 = 10 poison → player2 loses
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(10);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        // Life should still be 20
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Mixed attackers: infect deals poison, non-infect deals life loss")
    void mixedAttackersInfectAndNonInfect() {
        harness.setLife(player2, 20);

        // Blackcleave Goblin (infect 2/1)
        Permanent infectAttacker = addCreatureReady(player1, new BlackcleaveGoblin());
        infectAttacker.setAttacking(true);

        // Carapace Forger (non-infect 2/2)
        Permanent normalAttacker = addCreatureReady(player1, new CarapaceForger());
        normalAttacker.setAttacking(true);

        resolveCombat();

        // Carapace Forger deals 2 regular damage → life goes from 20 to 18
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        // Blackcleave Goblin deals 2 poison counters
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(2);
    }

    @Test
    @DisplayName("Haste allows Blackcleave Goblin to attack the turn it resolves")
    void attacksOnTurnItResolves() {
        harness.setHand(player1, List.of(new BlackcleaveGoblin()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Infect leaves counters on a surviving blocker without marking damage")
    void survivingBlockerGetsCountersInsteadOfMarkedDamage() {
        Permanent attacker = addCreatureReady(player1, new BlackcleaveGoblin());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new AlphaTyrranax());
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Alpha Tyrranax");
        assertThat(blocker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(blocker.getMarkedDamage()).isZero();
        assertThat(gqs.getEffectivePower(gd, blocker)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, blocker)).isEqualTo(3);
        harness.assertInGraveyard(player1, "Blackcleave Goblin");
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
    }
}
