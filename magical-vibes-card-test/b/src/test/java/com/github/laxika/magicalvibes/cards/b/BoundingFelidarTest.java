package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.s.SterlingKeykeeper;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BoundingFelidar.class, SterlingKeykeeper.class})
class BoundingFelidarTest extends BaseCardTest {

    @Test
    @DisplayName("Saddle 2 taps another creature and saddles Bounding Felidar")
    void saddleTapsAnotherCreature() {
        Permanent felidar = addCreatureReady(player1, new BoundingFelidar());
        Permanent helper = addCreatureReady(player1, new SterlingKeykeeper());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(felidar.isSaddled()).isTrue();
        assertThat(helper.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Attacking while saddled puts counters on other creatures and gains life for each")
    void attacksWhileSaddled() {
        Permanent felidar = addCreatureReady(player1, new BoundingFelidar());
        Permanent firstHelper = addCreatureReady(player1, new SterlingKeykeeper());
        Permanent secondHelper = addCreatureReady(player1, new SterlingKeykeeper());
        felidar.setSaddled(true);

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(felidar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(firstHelper.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(secondHelper.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("The attack trigger checks saddled when attackers are declared")
    void doesNotTriggerWhenNotSaddledAtDeclaration() {
        Permanent felidar = addCreatureReady(player1, new BoundingFelidar());
        Permanent helper = addCreatureReady(player1, new SterlingKeykeeper());

        declareAttackers(player1, List.of(0));
        felidar.setSaddled(true);
        resolveAllTriggers();

        assertThat(helper.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    void saddledAttackWithoutOtherCreaturesGainsNoLife() {
        Permanent felidar = addCreatureReady(player1, new BoundingFelidar());
        felidar.setSaddled(true);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(felidar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    void saddledAttackDoesNotCountOpposingCreatures() {
        Permanent felidar = addCreatureReady(player1, new BoundingFelidar());
        Permanent ally = addCreatureReady(player1, new SterlingKeykeeper());
        Permanent opponent = addCreatureReady(player2, new SterlingKeykeeper());
        felidar.setSaddled(true);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(ally.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    void summoningSickCreatureCanPaySaddleCost() {
        Permanent felidar = addCreatureReady(player1, new BoundingFelidar());
        Permanent helper = addCreatureReady(player1, new SterlingKeykeeper());
        helper.setSummoningSick(true);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(helper.isTapped()).isTrue();
        assertThat(felidar.isSaddled()).isTrue();
        assertThat(felidar.isTapped()).isFalse();
    }

    @Test
    void cannotSaddleUsingFelidarsOwnPower() {
        Permanent felidar = addCreatureReady(player1, new BoundingFelidar());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(felidar.isTapped()).isFalse();
        assertThat(felidar.isSaddled()).isFalse();
    }

    @Test
    void cannotSaddleDuringCombat() {
        Permanent felidar = addCreatureReady(player1, new BoundingFelidar());
        Permanent helper = addCreatureReady(player1, new SterlingKeykeeper());
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(helper.isTapped()).isFalse();
        assertThat(felidar.isSaddled()).isFalse();
    }

    @Test
    void attackTriggerResolvesAfterFelidarLeavesBattlefield() {
        Permanent felidar = addCreatureReady(player1, new BoundingFelidar());
        Permanent helper = addCreatureReady(player1, new SterlingKeykeeper());
        felidar.setSaddled(true);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(felidar);
        gd.playerGraveyards.get(player1.getId()).add(felidar.getCard());
        resolveAllTriggers();

        assertThat(helper.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    void attackTriggerCountsCreaturesAtResolution() {
        Permanent felidar = addCreatureReady(player1, new BoundingFelidar());
        felidar.setSaddled(true);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        assertThat(gd.stack).hasSize(1);
        Permanent newcomer = addCreatureReady(player1, new SterlingKeykeeper());
        resolveAllTriggers();

        assertThat(newcomer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(felidar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }
}
