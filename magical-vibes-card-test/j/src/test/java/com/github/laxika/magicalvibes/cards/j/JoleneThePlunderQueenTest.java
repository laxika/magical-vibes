package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.e.EvolutionSage;
import com.github.laxika.magicalvibes.cards.t.Treasure;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JoleneThePlunderQueen.class, EvolutionSage.class, Treasure.class})
class JoleneThePlunderQueenTest extends BaseCardTest {

    @Test
    void createsTwoTreasuresWhenAttackingAnOpponent() {
        addCreatureReady(player1, new JoleneThePlunderQueen());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(2);
    }

    @Test
    void doesNotCreateTreasureWhenOpponentAttacksJolene() {
        addCreatureReady(player1, new JoleneThePlunderQueen());
        addCreatureReady(player2, new EvolutionSage());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(countPermanents(player2, "Treasure")).isZero();
        assertThat(countPermanents(player1, "Treasure")).isZero();
    }

    @Test
    void sacrificesFiveTreasuresForFiveCounters() {
        Permanent jolene = addCreatureReady(player1, new JoleneThePlunderQueen());
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player1, new Treasure());
        }

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Treasure")).isZero();
        assertThat(jolene.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @Test
    void triggersOnlyOnceWhenAttackingMultipleOpponents() {
        Player thirdPlayer = addOpponent();
        addCreatureReady(player1, new JoleneThePlunderQueen());
        addCreatureReady(player1, new EvolutionSage());

        declareAttackersAt(player1, Map.of(0, player2.getId(), 1, thirdPlayer.getId()));
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(2);
    }

    @Test
    void attackingOpponentCreatesOneTreasureWhenAttackingAnotherOpponent() {
        Player thirdPlayer = addOpponent();
        addCreatureReady(player1, new JoleneThePlunderQueen());
        addCreatureReady(player2, new EvolutionSage());

        declareAttackersAt(player2, Map.of(0, thirdPlayer.getId()));
        resolveAllTriggers();

        assertThat(countPermanents(player2, "Treasure")).isEqualTo(1);
        assertThat(countPermanents(player1, "Treasure")).isZero();
    }

    @Test
    void multipleCreaturesAttackingOneOpponentCreateOnlyTwoTreasures() {
        addCreatureReady(player1, new JoleneThePlunderQueen());
        addCreatureReady(player1, new EvolutionSage());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(2);
    }

    @Test
    void cannotPayWithOpponentsTreasures() {
        Permanent jolene = addCreatureReady(player1, new JoleneThePlunderQueen());
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new Treasure());
        }
        harness.addToBattlefield(player2, new Treasure());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(4);
        assertThat(countPermanents(player2, "Treasure")).isEqualTo(1);
        assertThat(jolene.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void tappedSummoningSickJoleneCanSacrificeTappedTreasures() {
        Permanent jolene = harness.addToBattlefieldAndReturn(player1, new JoleneThePlunderQueen());
        jolene.setSummoningSick(true);
        jolene.tap();
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefieldAndReturn(player1, new Treasure()).tap();
        }

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Treasure")).isZero();
        assertThat(jolene.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    private void declareAttackersAt(Player attacker, Map<Integer, UUID> targets) {
        harness.forceActivePlayer(attacker);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, attacker, targets.keySet().stream().sorted().toList(), targets);
    }

    private Player addOpponent() {
        Player opponent = new Player(UUID.randomUUID(), "Charlie");
        UUID id = opponent.getId();
        gd.playerIds.add(id);
        gd.orderedPlayerIds.add(id);
        gd.playerNames.add("Charlie");
        gd.playerIdToName.put(id, "Charlie");
        gd.playerDecks.put(id, new ArrayList<>());
        gd.playerHands.put(id, new ArrayList<>());
        gd.playerBattlefields.put(id, new ArrayList<>());
        gd.playerGraveyards.put(id, new ArrayList<>());
        gd.playerCommandZones.put(id, new ArrayList<>());
        gd.playerManaPools.put(id, new ManaPool());
        gd.playerLifeTotals.put(id, 20);
        return opponent;
    }

}
