package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.y.YouthfulKnight;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpittingHydra.class, YouthfulKnight.class})
class SpittingHydraTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield with four +1/+1 counters")
    void entersWithFourPlusOneCounters() {
        harness.castFromHand(player1, new SpittingHydra(), "{3}{R}{R}");
        harness.passBothPriorities();

        Permanent hydra = findPermanent(player1, "Spitting Hydra");
        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Removes a +1/+1 counter to deal 1 damage to target creature")
    void removesCounterAndDealsDamageToCreature() {
        Permanent hydra = harness.enterBattlefieldAndReturn(player1, new SpittingHydra());
        Permanent target = addCreatureReady(player2, new YouthfulKnight());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Can target itself")
    void canTargetItself() {
        Permanent hydra = harness.enterBattlefieldAndReturn(player1, new SpittingHydra());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, hydra.getId());
        harness.passBothPriorities();

        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(hydra.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate without a +1/+1 counter")
    void cannotActivateWithoutCounter() {
        Permanent hydra = harness.addToBattlefieldAndReturn(player1, new SpittingHydra());
        hydra.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        Permanent target = addCreatureReady(player2, new YouthfulKnight());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough counters");
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        Permanent hydra = harness.enterBattlefieldAndReturn(player1, new SpittingHydra());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Invalid target permanent");

        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }
    @Test
    @DisplayName("Counter is paid immediately and ability works while tapped on the opponent's turn")
    void paysCounterBeforeResolutionWhileTappedOnOpponentsTurn() {
        Permanent hydra = harness.enterBattlefieldAndReturn(player1, new SpittingHydra());
        Permanent target = harness.enterBattlefieldAndReturn(player2, new SpittingHydra());
        hydra.tap();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(target.getMarkedDamage()).isZero();

        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Ability still deals damage after removing the last counter kills its source")
    void lastCounterActivationResolvesAfterSourceDies() {
        Permanent hydra = harness.enterBattlefieldAndReturn(player1, new SpittingHydra());
        Permanent target = harness.enterBattlefieldAndReturn(player2, new SpittingHydra());
        harness.addMana(player1, ManaColor.RED, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        for (int activation = 0; activation < 3; activation++) {
            harness.activateAbility(player1, 0, null, target.getId());
            harness.passBothPriorities();
        }

        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.getMarkedDamage()).isEqualTo(3);

        harness.activateAbility(player1, 0, null, target.getId());

        harness.assertNotOnBattlefield(player1, "Spitting Hydra");
        harness.assertInGraveyard(player1, "Spitting Hydra");
        harness.assertOnBattlefield(player2, "Spitting Hydra");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Spitting Hydra");
        harness.assertInGraveyard(player2, "Spitting Hydra");
    }
}
