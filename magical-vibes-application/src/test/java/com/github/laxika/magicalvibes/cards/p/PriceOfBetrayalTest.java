package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.d.DreadhordeInvasion;
import com.github.laxika.magicalvibes.cards.j.JaceWielderOfMysteries;
import com.github.laxika.magicalvibes.cards.m.ManaGeode;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PriceOfBetrayal.class, GrizzlyBears.class, DreadhordeInvasion.class,
        JaceWielderOfMysteries.class, ManaGeode.class})
class PriceOfBetrayalTest extends BaseCardTest {

    @Test
    void removesChosenCountersOfDifferentKindsFromTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        target.setCounterCount(CounterType.CHARGE, 2);

        castPriceOfBetrayal(target.getId());

        harness.handleListChoice(player1, "2");
        harness.handleListChoice(player1, "1");

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    void removesUpToFivePoisonAndEnergyCountersFromTargetOpponent() {
        gd.playerPoisonCounters.put(player2.getId(), 4);
        gd.playerEnergyCounters.put(player2.getId(), 3);

        castPriceOfBetrayal(player2.getId());

        harness.handleListChoice(player1, "4");
        harness.handleListChoice(player1, "1");

        assertThat(gd.playerPoisonCounters.get(player2.getId())).isZero();
        assertThat(gd.playerEnergyCounters.get(player2.getId())).isEqualTo(2);
    }

    @Test
    void cannotTargetItsController() {
        assertThatThrownBy(() -> castPriceOfBetrayal(player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void mayRemoveZeroCountersFromOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        target.setCounterCount(CounterType.CHARGE, 2);

        castPriceOfBetrayal(target.getId());
        harness.handleListChoice(player1, "0");
        harness.handleListChoice(player1, "0");

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(target.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotRemoveMoreThanFiveCountersAcrossKinds() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.setCounterCount(CounterType.CHARGE, 4);
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);

        castPriceOfBetrayal(target.getId());
        harness.handleListChoice(player1, "4");
        assertThatThrownBy(() -> harness.handleListChoice(player1, "2"))
                .isInstanceOf(IllegalArgumentException.class);
        harness.handleListChoice(player1, "1");

        assertThat(target.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void removesCountersFromNoncreatureArtifact() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ManaGeode());
        target.setCounterCount(CounterType.CHARGE, 7);

        castPriceOfBetrayal(target.getId());
        harness.handleListChoice(player1, "5");

        assertThat(target.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
    }

    @Test
    void removingAllLoyaltyCountersPutsPlaneswalkerInGraveyard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new JaceWielderOfMysteries());
        target.setCounterCount(CounterType.LOYALTY, 4);

        castPriceOfBetrayal(target.getId());
        harness.handleListChoice(player1, "4");

        harness.assertNotOnBattlefield(player2, "Jace, Wielder of Mysteries");
        harness.assertInGraveyard(player2, "Jace, Wielder of Mysteries");
    }

    @Test
    void canTargetCreatureWithNoCounters() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castPriceOfBetrayal(target.getId());

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Price of Betrayal");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotTargetEnchantmentThatIsNotAnArtifactCreatureOrPlaneswalker() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DreadhordeInvasion());
        target.setCounterCount(CounterType.CHARGE, 2);

        assertThatThrownBy(() -> castPriceOfBetrayal(target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void removesRadiationCountersFromOpponent() {
        gd.playerRadCounters.put(player2.getId(), 3);

        castPriceOfBetrayal(player2.getId());
        harness.handleListChoice(player1, "3");

        assertThat(gd.playerRadCounters.get(player2.getId())).isZero();
    }

    @Test
    void removesExperienceCountersFromOpponent() {
        gd.playerExperienceCounters.put(player2.getId(), 3);

        castPriceOfBetrayal(player2.getId());
        harness.handleListChoice(player1, "3");

        assertThat(gd.playerExperienceCounters.get(player2.getId())).isZero();
    }

    @Test
    void removesSparkCountersFromOpponent() {
        gd.playerSparkCounters.put(player2.getId(), 3);

        castPriceOfBetrayal(player2.getId());
        harness.handleListChoice(player1, "3");

        assertThat(gd.playerSparkCounters.get(player2.getId())).isZero();
    }

    private void castPriceOfBetrayal(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new PriceOfBetrayal()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castAndResolveSorcery(player1, 0, targetId);
    }
}
