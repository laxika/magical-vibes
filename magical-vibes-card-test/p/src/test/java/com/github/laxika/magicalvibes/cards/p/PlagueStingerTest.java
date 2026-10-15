package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PlagueStinger.class, AirElemental.class, Memnite.class})
class PlagueStingerTest extends BaseCardTest {

    @Test
    @DisplayName("Plague Stinger resolves onto the battlefield")
    void resolvesOntoBattlefield() {
        harness.setHand(player1, List.of(new PlagueStinger()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Plague Stinger");
    }

    @Test
    @DisplayName("Unblocked Plague Stinger deals 1 poison counter instead of life loss")
    void dealsPoisonCountersWhenUnblocked() {
        harness.setLife(player2, 20);

        Permanent atkPerm = addCreatureReady(player1, new PlagueStinger());
        atkPerm.setAttacking(true);
        resolveCombat();

        // Life should remain unchanged
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        // Poison counters should equal power (1)
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }

    @Test
    @DisplayName("Blocked Plague Stinger deals -1/-1 counters to blocker instead of regular damage")
    void dealsMinusCountersToBlocker() {
        // Air Elemental is 4/4 with flying — can block Plague Stinger
        Permanent blockerPerm = addCreatureReady(player2, new AirElemental());

        // Plague Stinger is 1/1 with flying + infect
        Permanent atkPerm = addCreatureReady(player1, new PlagueStinger());
        atkPerm.setAttacking(true);
        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(atkPerm);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));
        harness.passBothPriorities();

        // Plague Stinger (1/1) dies to Air Elemental (4/4)
        harness.assertNotOnBattlefield(player1, "Plague Stinger");
        harness.assertInGraveyard(player1, "Plague Stinger");

        // Air Elemental should have 1 -1/-1 counter (from 1 infect damage), making it 3/3 — survives
        harness.assertOnBattlefield(player2, "Air Elemental");
        Permanent elemental = findPermanent(player2, "Air Elemental");
        assertThat(elemental.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);

        // No poison counters — damage went to a creature
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(0);
    }

    @Test
    @DisplayName("A creature without flying or reach cannot block Plague Stinger")
    void groundCreatureCannotBlock() {
        Permanent attacker = addCreatureReady(player1, new PlagueStinger());
        attacker.setAttacking(true);
        addCreatureReady(player2, new Memnite());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    @DisplayName("Plague Stinger can block a ground creature and kills it with infect")
    void canBlockGroundCreature() {
        Permanent attacker = addCreatureReady(player1, new Memnite());
        attacker.setAttacking(true);
        addCreatureReady(player2, new PlagueStinger());
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Memnite");
        harness.assertInGraveyard(player1, "Memnite");
        harness.assertNotOnBattlefield(player2, "Plague Stinger");
        harness.assertInGraveyard(player2, "Plague Stinger");
        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Plague Stinger deals infect damage while blocking")
    void blockingDealsMinusCountersToAttacker() {
        Permanent attacker = addCreatureReady(player1, new AirElemental());
        attacker.setAttacking(true);
        addCreatureReady(player2, new PlagueStinger());
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Plague Stinger");
        harness.assertOnBattlefield(player1, "Air Elemental");
        assertThat(attacker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(attacker.getMarkedDamage()).isZero();
        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
    }
}
