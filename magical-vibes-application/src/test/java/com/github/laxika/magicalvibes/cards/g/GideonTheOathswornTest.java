package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GideonTheOathsworn.class, GrizzlyBears.class, FountainOfYouth.class, Shock.class})
class GideonTheOathswornTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with two non-Gideon creatures puts counters on those creatures")
    void attackTriggerCountersNonGideonAttackers() {
        Permanent gideon = addReadyGideon(4);
        Permanent firstBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondBear = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        declareAttackers(player1, List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(gideon),
                gd.playerBattlefields.get(player1.getId()).indexOf(firstBear),
                gd.playerBattlefields.get(player1.getId()).indexOf(secondBear)));
        resolveAllTriggers();

        assertThat(firstBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
        assertThat(secondBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
        assertThat(gideon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The attack trigger requires two non-Gideon creatures")
    void attackTriggerRequiresTwoNonGideonCreatures() {
        Permanent gideon = addReadyGideon(4);
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        declareAttackers(player1, List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(gideon),
                gd.playerBattlefields.get(player1.getId()).indexOf(bear)));
        resolveAllTriggers();

        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("+2 animates Gideon and prevents damage to him until end of turn")
    void plusTwoAnimatesAndPreventsDamage() {
        Permanent gideon = addReadyGideon(4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, gideon)).isTrue();
        assertThat(gqs.getEffectivePower(gd, gideon)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, gideon)).isEqualTo(5);

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castInstant(player2, 0, gideon.getId());
        harness.passBothPriorities();

        assertThat(gideon.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
    }

    @Test
    @DisplayName("-9 exiles Gideon and each opponent creature")
    void minusNineExilesGideonAndOpponentCreatures() {
        Permanent gideon = addReadyGideon(10);
        Permanent ownBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentBear = addCreatureReady(player2, new GrizzlyBears());
        Permanent opponentFountain = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownBear);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(gideon);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponentBear);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentFountain);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(gideon.getCard());
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(opponentBear.getCard());
    }

    private Permanent addReadyGideon(int loyalty) {
        Permanent gideon = harness.addToBattlefieldAndReturn(player1, new GideonTheOathsworn());
        gideon.setCounterCount(CounterType.LOYALTY, loyalty);
        gideon.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return gideon;
    }
}
