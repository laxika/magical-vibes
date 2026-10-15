package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.ContagiousVorrac;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BlightbellyRat.class, ContagiousVorrac.class})
class BlightbellyRatTest extends BaseCardTest {

    @Test
    @DisplayName("Toxic deals combat damage and gives the player a poison counter")
    void toxicDealsCombatDamageAndPoison() {
        harness.setLife(player2, 20);

        Permanent attacker = addCreatureReady(player1, new BlightbellyRat());
        attacker.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }

    @Test
    @DisplayName("When Blightbelly Rat dies, its proliferate trigger adds a counter")
    void deathTriggerProliferates() {
        Permanent rat = addCreatureReady(player1, new BlightbellyRat());
        rat.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new ContagiousVorrac());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        Permanent creatureWithCounter = harness.addToBattlefieldAndReturn(player2, new ContagiousVorrac());
        creatureWithCounter.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(creatureWithCounter.getId()));

        assertThat(creatureWithCounter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Toxic gives poison with combat damage without using the stack")
    void toxicDoesNotUseStack() {
        Permanent rat = addCreatureReady(player1, new BlightbellyRat());
        rat.setAttacking(true);
        harness.forceActivePlayer(player1);

        harness.resolveCombatDamage();

        harness.assertLife(player2, 18);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Increasing damage does not increase toxic's poison counters")
    void increasedDamageStillGivesOnePoisonCounter() {
        Permanent rat = addCreatureReady(player1, new BlightbellyRat());
        rat.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        rat.setAttacking(true);
        harness.forceActivePlayer(player1);

        harness.resolveCombatDamage();

        harness.assertLife(player2, 16);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }

    @Test
    @DisplayName("Death proliferates all existing counter kinds and chosen players only")
    void deathProliferatesChosenPermanentsAndPlayers() {
        Permanent chosen = harness.addToBattlefieldAndReturn(player1, new ContagiousVorrac());
        chosen.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        chosen.setCounterCount(CounterType.CHARGE, 3);
        Permanent unchosen = harness.addToBattlefieldAndReturn(player2, new ContagiousVorrac());
        unchosen.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        gd.playerPoisonCounters.put(player1.getId(), 2);
        gd.playerPoisonCounters.put(player2.getId(), 3);

        killRatInCombat();
        harness.handleMultiplePermanentsChosen(player1, List.of(chosen.getId(), player2.getId()));

        assertThat(chosen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(chosen.getCounterCount(CounterType.CHARGE)).isEqualTo(4);
        assertThat(unchosen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerPoisonCounters.get(player1.getId())).isEqualTo(2);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(4);
    }

    @Test
    @DisplayName("Proliferate can choose nothing and blocked toxic gives no poison")
    void deathCanChooseNothing() {
        gd.playerPoisonCounters.put(player1.getId(), 2);

        killRatInCombat();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(gd.playerPoisonCounters.get(player1.getId())).isEqualTo(2);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Death proliferate resolves when nothing has counters")
    void deathWithNoCounters() {
        killRatInCombat();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Proliferate includes players with energy or experience and adds every kind")
    void deathProliferatesEveryPlayerCounterKind() {
        gd.playerEnergyCounters.put(player1.getId(), 2);
        gd.playerExperienceCounters.put(player1.getId(), 3);
        gd.playerPoisonCounters.put(player2.getId(), 1);
        gd.playerEnergyCounters.put(player2.getId(), 4);
        gd.playerExperienceCounters.put(player2.getId(), 5);

        killRatInCombat();
        harness.handleMultiplePermanentsChosen(player1, List.of(player1.getId(), player2.getId()));

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(3);
        assertThat(gd.playerExperienceCounters.get(player1.getId())).isEqualTo(4);
        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(2);
        assertThat(gd.playerEnergyCounters.get(player2.getId())).isEqualTo(5);
        assertThat(gd.playerExperienceCounters.get(player2.getId())).isEqualTo(6);
    }

    private void killRatInCombat() {
        Permanent rat = addCreatureReady(player1, new BlightbellyRat());
        rat.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new ContagiousVorrac());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(gd.playerBattlefields.get(player1.getId()).indexOf(rat));

        resolveCombat();
        harness.assertInGraveyard(player1, "Blightbelly Rat");
        harness.passBothPriorities();
    }
}
