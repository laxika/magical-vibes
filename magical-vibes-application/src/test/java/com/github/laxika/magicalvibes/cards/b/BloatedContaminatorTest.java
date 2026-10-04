package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.Cankerbloom;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BloatedContaminator.class, GrizzlyBears.class, SerraAngel.class, Cankerbloom.class})
class BloatedContaminatorTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage gives poison and triggers proliferate")
    void combatDamageGivesPoisonAndProliferates() {
        harness.setLife(player2, 20);

        Permanent contaminator = harness.addToBattlefieldAndReturn(player1, new BloatedContaminator());
        contaminator.setSummoningSick(false);
        contaminator.setAttacking(true);

        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        resolveCombat();
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Blocked combat damage does not give poison or proliferate")
    void blockedCombatDamageDoesNotTrigger() {
        harness.setLife(player2, 20);

        Permanent contaminator = harness.addToBattlefieldAndReturn(player1, new BloatedContaminator());
        contaminator.setSummoningSick(false);
        contaminator.setAttacking(true);

        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
        assertThat(bears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Toxic gives poison as combat damage is dealt, before triggered abilities resolve")
    void toxicIsImmediateCombatDamageResult() {
        Permanent contaminator = harness.addToBattlefieldAndReturn(player1, new BloatedContaminator());
        contaminator.setSummoningSick(false);
        contaminator.setAttacking(true);

        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }

    @Test
    @DisplayName("Proliferate can increase the first poison counter from this combat damage")
    void proliferatesFirstPoisonCounter() {
        Permanent contaminator = harness.addToBattlefieldAndReturn(player1, new BloatedContaminator());
        contaminator.setSummoningSick(false);
        contaminator.setAttacking(true);
        Permanent fungus = harness.addToBattlefieldAndReturn(player1, new Cankerbloom());
        fungus.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        resolveCombat();
        resolveAllTriggers();
        harness.handleMultiplePermanentsChosen(player1, List.of(player2.getId(), fungus.getId()));
        resolveAllTriggers();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(2);
        assertThat(fungus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Trample damage gives one poison counter and allows choosing no proliferate recipients")
    void trampleDamageWithEmptyProliferateSelection() {
        Permanent contaminator = harness.addToBattlefieldAndReturn(player1, new BloatedContaminator());
        contaminator.setCounterCount(CounterType.CHARGE, 1);
        contaminator.setSummoningSick(false);
        contaminator.setAttacking(true);
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new Cankerbloom());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(0), List.of(0));
        resolveCombat();
        resolveAllTriggers();
        harness.handleMultiplePermanentsChosen(player1, List.of());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(contaminator.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }
}
