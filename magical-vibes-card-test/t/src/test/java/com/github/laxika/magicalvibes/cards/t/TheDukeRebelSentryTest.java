package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.FrogSquirrels;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheDukeRebelSentry.class, FrogSquirrels.class})
class TheDukeRebelSentryTest extends BaseCardTest {

    @Test
    void entersWithPlusOnePlusOneCounter() {
        Permanent duke = harness.enterBattlefieldAndReturn(player1, new TheDukeRebelSentry());

        assertThat(duke.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void removesCounterToBoostAnotherCreatureAndGrantHexproof() {
        Permanent duke = addDuke();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new FrogSquirrels());

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(duke.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    void hexproofWearsOffAtEndOfTurn() {
        addDuke();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new FrogSquirrels());

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    void cannotTargetTheDukeOrAnOpponentCreature() {
        Permanent duke = addDuke();
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new FrogSquirrels());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, duke.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(duke.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void paysTapAndCounterCostsBeforeTheAbilityResolves() {
        Permanent duke = addDuke();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new FrogSquirrels());

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(duke.isTapped()).isTrue();
        assertThat(duke.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, target, Keyword.HEXPROOF)).isFalse();

        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    void canRemoveACounterOtherThanPlusOnePlusOne() {
        Permanent duke = addCreatureReady(player1, new TheDukeRebelSentry());
        duke.setCounterCount(CounterType.CHARGE, 1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new FrogSquirrels());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(duke.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    void controllerChoosesWhichCounterTypeToRemove() {
        Permanent duke = addDuke();
        duke.setCounterCount(CounterType.CHARGE, 1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new FrogSquirrels());

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(gd.interaction.isAwaitingInput())
                .as("The controller must be able to choose between the +1/+1 and charge counters")
                .isTrue();
        assertThat(duke.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(duke.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    void cannotActivateWithoutACounter() {
        Permanent duke = addCreatureReady(player1, new TheDukeRebelSentry());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new FrogSquirrels());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(duke.isTapped()).isFalse();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        Permanent duke = harness.enterBattlefieldAndReturn(player1, new TheDukeRebelSentry());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new FrogSquirrels());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(duke.isTapped()).isFalse();
        assertThat(duke.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent duke = addDuke();
        duke.tap();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new FrogSquirrels());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(duke.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void abilityResolvesAfterTheDukeLeavesTheBattlefield() {
        Permanent duke = addDuke();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new FrogSquirrels());

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(duke);
        gd.playerGraveyards.get(player1.getId()).add(duke.getCard());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    void abilityDoesNotResolveIfOpponentGainsControlOfTarget() {
        Permanent duke = addDuke();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new FrogSquirrels());

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerBattlefields.get(player2.getId()).add(target);
        harness.passBothPriorities();

        assertThat(duke.isTapped()).isTrue();
        assertThat(duke.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, target, Keyword.HEXPROOF)).isFalse();
    }

    private Permanent addDuke() {
        Permanent duke = addCreatureReady(player1, new TheDukeRebelSentry());
        duke.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        return duke;
    }
}
