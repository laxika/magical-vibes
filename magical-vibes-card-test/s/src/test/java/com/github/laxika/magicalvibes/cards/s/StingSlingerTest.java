package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StingSlinger.class})
class StingSlingerTest extends BaseCardTest {

    @Test
    void blightsCreatureAndDealsDamageToEachOpponent() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Permanent stingSlinger = addCreatureReady(player1, new StingSlinger());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new StingSlinger());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int startingLife = gd.getLife(player2.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handlePermanentChosen(player1, creature.getId());

        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(stingSlinger.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(gd.getLife(player2.getId())).isEqualTo(startingLife);
        assertThat(stingSlinger.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(startingLife - 2);
        harness.assertLife(player1, 20);
    }

    @Test
    void canBlightItselfAndPaysBeforeOpponentsCanRespond() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent stingSlinger = addCreatureReady(player1, new StingSlinger());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(stingSlinger.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(stingSlinger.isTapped()).isTrue();
        harness.assertLife(player2, 20);

        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
        assertThat(stingSlinger.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    void lethalBlightCostDoesNotStopAbilityFromDealingDamage() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent stingSlinger = addCreatureReady(player1, new StingSlinger());
        stingSlinger.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Sting-Slinger");
        harness.assertInGraveyard(player1, "Sting-Slinger");
        harness.assertLife(player2, 20);

        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent stingSlinger = harness.addToBattlefieldAndReturn(player1, new StingSlinger());
        stingSlinger.setSummoningSick(true);
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(stingSlinger.isTapped()).isFalse();
        assertThat(stingSlinger.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        harness.assertLife(player2, 20);
    }

    @Test
    void cannotPayActivationWithOnlyColorlessMana() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent stingSlinger = addCreatureReady(player1, new StingSlinger());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(stingSlinger.isTapped()).isFalse();
        assertThat(stingSlinger.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        harness.assertLife(player2, 20);
    }
}
