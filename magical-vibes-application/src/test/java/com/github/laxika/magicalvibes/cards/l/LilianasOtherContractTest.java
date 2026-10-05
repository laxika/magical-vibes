package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LilianasOtherContract.class, LilianasUndeadMinion.class, GrizzlyBears.class})
class LilianasOtherContractTest extends BaseCardTest {

    @Test
    void entersDrawsThreeAndLosesThreeLife() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.castFromHand(player1, new LilianasOtherContract(), "{4}{B}");
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    @Test
    void replacesGameLossByReturningTransformed() {
        Permanent contract = harness.addToBattlefieldAndReturn(player1, new LilianasOtherContract());
        harness.setLife(player1, 0);

        harness.runStateBasedActions();

        Permanent minion = findPermanent(player1, "Liliana's Undead Minion");
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        assertThat(minion).isNotSameAs(contract);
        assertThat(minion.isTransformed()).isTrue();
        assertThat(minion.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(gd.getLife(player1.getId())).isZero();
    }

    @Test
    void plusOneMakesEachOpponentLoseOneLife() {
        Permanent minion = addTransformedMinion();
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        assertThat(minion.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
    }

    @Test
    void minusFourDestroysTargetCreature() {
        Permanent minion = addTransformedMinion();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(minion.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    void entryLifeLossReturnsContractTransformedWithoutDrawingAgain() {
        harness.setLibrary(player1, List.of(
                new LilianasOtherContract(), new LilianasOtherContract(), new LilianasOtherContract()));
        harness.setLife(player1, 3);
        harness.castFromHand(player1, new LilianasOtherContract(), "{4}{B}");
        resolveAllTriggers();
        harness.runStateBasedActions();

        harness.assertOnBattlefield(player1, "Liliana's Undead Minion");
        harness.assertNotOnBattlefield(player1, "Liliana's Other Contract");
        harness.assertLife(player1, 0);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    void drawingFromEmptyLibraryTransformsContractAndStillLosesThreeLife() {
        harness.setLibrary(player1, List.of());
        harness.castFromHand(player1, new LilianasOtherContract(), "{4}{B}");
        resolveAllTriggers();
        harness.runStateBasedActions();

        harness.assertOnBattlefield(player1, "Liliana's Undead Minion");
        harness.assertLife(player1, 17);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    void poisonLossReturnsContractTransformedAndMinionPreventsFurtherLoss() {
        harness.addToBattlefield(player1, new LilianasOtherContract());
        gd.playerPoisonCounters.put(player1.getId(), 10);

        harness.runStateBasedActions();
        harness.runStateBasedActions();

        harness.assertOnBattlefield(player1, "Liliana's Undead Minion");
        assertThat(gd.playerPoisonCounters.get(player1.getId())).isEqualTo(10);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    void minionPreventsItsControllerLosingButNotItsOpponent() {
        addTransformedMinion();
        harness.setLife(player1, 0);
        harness.runStateBasedActions();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);

        harness.setLife(player2, 0);
        harness.runStateBasedActions();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player1.getId());
    }
    private Permanent addTransformedMinion() {
        LilianasOtherContract front = new LilianasOtherContract();
        Permanent minion = harness.addToBattlefieldAndReturn(player1, front);
        minion.setCard(front.getBackFaceCard());
        minion.setTransformed(true);
        minion.setCounterCount(CounterType.LOYALTY, 5);
        minion.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        return minion;
    }
}
