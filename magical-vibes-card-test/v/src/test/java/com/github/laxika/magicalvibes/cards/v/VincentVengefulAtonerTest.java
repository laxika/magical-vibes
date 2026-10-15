package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.y.YunaGrandSummoner;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.FakeConnection;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VincentVengefulAtoner.class, YunaGrandSummoner.class})
class VincentVengefulAtonerTest extends BaseCardTest {

    @Test
    void putsOnlyOneCounterOnVincentWhenMultipleCreaturesDealCombatDamage() {
        Permanent vincent = addCreatureReady(player1, new VincentVengefulAtoner());
        addCreatureReady(player1, new YunaGrandSummoner());

        declareAttackers(List.of(0, 1));
        harness.passBothPriorities();

        assertThat(vincent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void chaosDoesNotRepeatDamageToTheOpponentThatWasHit() {
        Permanent vincent = addCreatureReady(player1, new VincentVengefulAtoner());
        vincent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(13);
    }

    @Test
    void allyDamageAddsCounterWhenVincentDoesNotAttack() {
        Permanent vincent = addCreatureReady(player1, new VincentVengefulAtoner());
        addCreatureReady(player1, new YunaGrandSummoner());

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(vincent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void opponentsCombatDamageDoesNotAddCounter() {
        Permanent vincent = addCreatureReady(player1, new VincentVengefulAtoner());
        Permanent attacker = addCreatureReady(player2, new YunaGrandSummoner());
        dealCombatDamage(player2, attacker, player1);
        resolveAllTriggers();

        assertThat(vincent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void chaosCanResolveAfterCombatCounterRaisesPowerFromSixToSeven() {
        Player third = addOpponent("Charlie");
        Permanent vincent = addCreatureReady(player1, new VincentVengefulAtoner());
        vincent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        dealCombatDamage(player1, vincent, player2);
        resolveAllTriggers();

        assertThat(vincent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 14);
        harness.assertLife(third, 14);
    }

    @Test
    void chaosBelowSevenAtResolutionDealsNoAdditionalDamage() {
        Player third = addOpponent("Charlie");
        Permanent vincent = addCreatureReady(player1, new VincentVengefulAtoner());
        dealCombatDamage(player1, vincent, player2);
        resolveAllTriggers();

        harness.assertLife(player2, 17);
        harness.assertLife(third, 20);
        assertThat(vincent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void chaosDealsOriginalDamageToEveryOtherOpponentWithoutRetriggering() {
        Player third = addOpponent("Charlie");
        Player fourth = addOpponent("Dana");
        Permanent vincent = addCreatureReady(player1, new VincentVengefulAtoner());
        vincent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
        dealCombatDamage(player1, vincent, player2);
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 13);
        harness.assertLife(third, 13);
        harness.assertLife(fourth, 13);
        assertThat(vincent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void chaosUsesLastKnownPowerWhenVincentLeavesBattlefield() {
        Player third = addOpponent("Charlie");
        Permanent vincent = addCreatureReady(player1, new VincentVengefulAtoner());
        vincent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
        dealCombatDamage(player1, vincent, player2);
        gd.playerBattlefields.get(player1.getId()).remove(vincent);
        gd.playerGraveyards.get(player1.getId()).add(vincent.getCard());
        resolveAllTriggers();

        harness.assertLife(player2, 13);
        harness.assertLife(third, 13);
    }

    @Test
    void chaosRechecksPowerAtResolution() {
        Player third = addOpponent("Charlie");
        Permanent vincent = addCreatureReady(player1, new VincentVengefulAtoner());
        vincent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
        dealCombatDamage(player1, vincent, player2);
        vincent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        resolveAllTriggers();

        harness.assertLife(player2, 13);
        harness.assertLife(third, 20);
    }

    @Test
    void simultaneousCombatDamageToDifferentPlayersAddsOneCounterPerPlayer() {
        Player third = addOpponent("Charlie");
        Permanent vincent = addCreatureReady(player1, new VincentVengefulAtoner());
        Permanent ally = addCreatureReady(player1, new YunaGrandSummoner());
        vincent.setAttacking(true);
        vincent.setAttackTarget(player2.getId());
        ally.setAttacking(true);
        ally.setAttackTarget(third.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        resolveAllTriggers();

        assertThat(vincent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertLife(player2, 17);
        harness.assertLife(third, 19);
    }

    private void dealCombatDamage(Player attackerController, Permanent attacker, Player defender) {
        attacker.setAttacking(true);
        attacker.setAttackTarget(defender.getId());
        harness.forceActivePlayer(attackerController);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
    }

    private Player addOpponent(String name) {
        UUID id = UUID.randomUUID();
        Player opponent = new Player(id, name);
        gd.playerIds.add(id);
        gd.orderedPlayerIds.add(id);
        gd.playerNames.add(name);
        gd.playerIdToName.put(id, name);
        gd.playerDecks.put(id, new ArrayList<>());
        gd.playerHands.put(id, new ArrayList<>());
        gd.playerBattlefields.put(id, new ArrayList<>());
        gd.playerGraveyards.put(id, new ArrayList<>());
        gd.playerCommandZones.put(id, new ArrayList<>());
        gd.playerManaPools.put(id, new ManaPool());
        gd.playerLifeTotals.put(id, 20);
        harness.getSessionManager().registerPlayer(new FakeConnection("conn-" + name), id, name);
        return opponent;
    }
}
