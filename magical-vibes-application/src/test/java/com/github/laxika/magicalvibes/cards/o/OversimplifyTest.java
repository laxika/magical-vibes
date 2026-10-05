package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.d.DoublingSeason;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.InnkeepersTalent;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Oversimplify.class, AirElemental.class, GrizzlyBears.class, Island.class,
        DoublingSeason.class, InnkeepersTalent.class})
class OversimplifyTest extends BaseCardTest {

    @Test
    void exilesCreaturesAndCreatesPerPlayerFractalsWithTheirTotalPower() {
        harness.addToBattlefield(player1, new AirElemental());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new Island());

        cast();

        harness.assertNotOnBattlefield(player1, "Air Elemental");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Island");

        Permanent playerOneFractal = findPermanent(player1, "Fractal");
        Permanent playerTwoFractal = findPermanent(player2, "Fractal");
        assertThat(playerOneFractal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
        assertThat(playerTwoFractal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(playerOneFractal.getEffectivePower()).isEqualTo(6);
        assertThat(playerTwoFractal.getEffectivePower()).isEqualTo(2);
    }

    @Test
    void countsCountersInLastKnownPowerAndPutsCreatureCardsInExile() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        cast();

        assertThat(findPermanent(player1, "Fractal").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(5);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(bears.getCard());
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Fractal");
    }

    @Test
    void createsZeroPowerFractalsEvenWhenNeitherPlayerHasCreatures() {
        cast();

        assertThat(gd.gameLog.stream().filter(entry -> entry.plainText()
                .contains("Fractal creature token enters the battlefield")).count()).isEqualTo(2);
        harness.assertNotOnBattlefield(player1, "Fractal");
        harness.assertNotOnBattlefield(player2, "Fractal");
        harness.assertInGraveyard(player1, "Oversimplify");
    }

    @Test
    void putsCountersOnBothFractalsCreatedByDoublingSeason() {
        harness.addToBattlefield(player1, new DoublingSeason());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        cast();

        assertThat(findPermanents(player1, "Fractal")).hasSize(2).allSatisfy(token ->
                assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4));
        assertThat(findPermanent(player2, "Fractal").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(2);
    }

    @Test
    void eachPlayerPutsTheirOwnCountersForInnkeepersTalent() {
        Permanent talent = harness.addToBattlefieldAndReturn(player1, new InnkeepersTalent());
        talent.setClassLevel(3);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        cast();

        assertThat(findPermanent(player1, "Fractal").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(4);
        assertThat(findPermanent(player2, "Fractal").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(2);
    }

    private void cast() {
        harness.castFromHand(player1, new Oversimplify(), "{3}{G}{U}");
        harness.passBothPriorities();
    }
}
