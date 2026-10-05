package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NissaVoiceOfZendikar.class, Forest.class, GrizzlyBears.class})
class NissaVoiceOfZendikarTest extends BaseCardTest {

    @Test
    @DisplayName("+1 creates a 0/1 green Plant creature token")
    void plusOneCreatesPlantToken() {
        Permanent nissa = addReadyNissa(3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent plant = findPermanent(player1, "Plant");
        assertThat(plant.getCard().isToken()).isTrue();
        assertThat(plant.getCard().getSubtypes()).containsExactly(CardSubtype.PLANT);
        assertThat(plant.getEffectivePower()).isZero();
        assertThat(plant.getEffectiveToughness()).isEqualTo(1);
        assertThat(nissa.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    @DisplayName("-2 puts a +1/+1 counter on each controlled creature only")
    void minusTwoPutsCountersOnOwnCreatures() {
        Permanent nissa = addReadyNissa(3);
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(ownBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
        assertThat(opponentBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(nissa.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    @DisplayName("-7 gains and draws the number of lands controlled")
    void minusSevenGainsLifeAndDrawsForLands() {
        Permanent nissa = addReadyNissa(7);
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 3);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 3);
        assertThat(nissa.getCounterCount(CounterType.LOYALTY)).isZero();
    }

    @Test
    @DisplayName("-2 includes creatures that enter before resolution and excludes noncreatures")
    void minusTwoCountsCreaturesAtResolution() {
        Permanent nissa = addReadyNissa(3);
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.activateAbility(player1, 0, 1, null, null);
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.passBothPriorities();

        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
        assertThat(forest.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(nissa.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("-7 counts lands at resolution even after Nissa leaves the battlefield")
    void minusSevenCountsLandsAtResolution() {
        addReadyNissa(7);
        harness.addToBattlefield(player1, new Forest());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 2, null, null);
        harness.assertNotOnBattlefield(player1, "Nissa, Voice of Zendikar");
        harness.assertInGraveyard(player1, "Nissa, Voice of Zendikar");
        harness.addToBattlefield(player1, new Forest());
        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore + 2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
    }

    @Test
    @DisplayName("-7 gains no life and draws no cards without controlled lands")
    void minusSevenWithNoLands() {
        addReadyNissa(7);
        harness.addToBattlefield(player2, new Forest());
        harness.setLibrary(player1, List.of());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private Permanent addReadyNissa(int loyalty) {
        Permanent nissa = harness.addToBattlefieldAndReturn(player1, new NissaVoiceOfZendikar());
        nissa.setCounterCount(CounterType.LOYALTY, loyalty);
        nissa.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return nissa;
    }
}
