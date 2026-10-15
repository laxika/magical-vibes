package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GoForTheThroat;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PhyrexianVatmother.class, GoForTheThroat.class})
class PhyrexianVatmotherTest extends BaseCardTest {

    @Test
    @DisplayName("Controller gets 1 poison counter at upkeep")
    void controllerGetsPoisonCounterAtUpkeep() {
        harness.addToBattlefield(player1, new PhyrexianVatmother());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isEqualTo(1);
    }

    @Test
    @DisplayName("Opponent does not get poison from controller's Vatmother upkeep trigger")
    void opponentDoesNotGetPoisonFromControllerUpkeep() {
        harness.addToBattlefield(player1, new PhyrexianVatmother());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(0);
    }

    @Test
    @DisplayName("Does not trigger during opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        harness.addToBattlefield(player1, new PhyrexianVatmother());

        advanceToUpkeep(player2); // opponent's upkeep
        harness.passBothPriorities();

        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isEqualTo(0);
    }

    @Test
    @DisplayName("Poison counters accumulate over multiple upkeeps")
    void poisonCountersAccumulate() {
        harness.addToBattlefield(player1, new PhyrexianVatmother());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isEqualTo(2);
    }

    @Test
    @DisplayName("Controller loses the game at 10 poison counters from Vatmother")
    void controllerLosesAtTenPoison() {
        harness.addToBattlefield(player1, new PhyrexianVatmother());
        gd.playerPoisonCounters.put(player1.getId(), 9);

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerPoisonCounters.get(player1.getId())).isEqualTo(10);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Poison counter from upkeep trigger is logged")
    void poisonCounterIsLogged() {
        harness.addToBattlefield(player1, new PhyrexianVatmother());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("gets 1 poison counter"));
    }

    @Test
    @DisplayName("Each Vatmother triggers separately and does not change life totals")
    void multipleVatmothersGiveSeparatePoisonCounters() {
        harness.addToBattlefield(player1, new PhyrexianVatmother());
        harness.addToBattlefield(player1, new PhyrexianVatmother());
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isZero();
        resolveAllTriggers();

        assertThat(gd.playerPoisonCounters.get(player1.getId())).isEqualTo(2);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Opponent's Vatmother poisons its own controller")
    void opponentControlledVatmotherPoisonsOpponent() {
        harness.addToBattlefield(player2, new PhyrexianVatmother());

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(1);
        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Removing Vatmother in response does not stop its upkeep trigger")
    void upkeepTriggerResolvesAfterSourceIsDestroyed() {
        Permanent vatmother = harness.addToBattlefieldAndReturn(player1, new PhyrexianVatmother());
        harness.setHand(player1, List.of(new GoForTheThroat()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        advanceToUpkeep(player1);
        harness.castInstant(player1, 0, vatmother.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Phyrexian Vatmother");
        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isZero();
        resolveAllTriggers();

        assertThat(gd.playerPoisonCounters.get(player1.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("Unblocked infect damage gives poison instead of life loss")
    void combatDamageToPlayerGivesPoison() {
        Permanent vatmother = harness.addToBattlefieldAndReturn(player1, new PhyrexianVatmother());
        vatmother.setSummoningSick(false);
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();

        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(4);
        harness.assertLife(player2, 20);
        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Infect combat damage puts counters on creatures simultaneously")
    void combatDamageToCreatureGivesMinusCounters() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new PhyrexianVatmother());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new PhyrexianVatmother());
        attacker.setSummoningSick(false);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(attacker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(4);
        assertThat(blocker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(4);
        assertThat(attacker.getMarkedDamage()).isZero();
        assertThat(blocker.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Phyrexian Vatmother");
        harness.assertOnBattlefield(player2, "Phyrexian Vatmother");
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
    }
}
