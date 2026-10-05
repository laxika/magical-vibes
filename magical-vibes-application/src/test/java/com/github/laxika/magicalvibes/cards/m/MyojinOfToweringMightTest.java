package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MyojinOfToweringMight.class, GrizzlyBears.class})
class MyojinOfToweringMightTest extends BaseCardTest {

    @Test
    @DisplayName("Cast from hand enters with an indestructible counter and indestructible")
    void castFromHandEntersWithIndestructibleCounter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player1, new MyojinOfToweringMight(), "{5}{G}{G}{G}");
        harness.passBothPriorities();

        Permanent myojin = findPermanent(player1, "Myojin of Towering Might");
        assertThat(myojin.getCounterCount(CounterType.INDESTRUCTIBLE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, myojin, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Entering without being cast from hand does not get an indestructible counter")
    void enteringWithoutCastingDoesNotGetIndestructibleCounter() {
        Permanent myojin = harness.enterBattlefieldAndReturn(player1, new MyojinOfToweringMight());

        assertThat(myojin.getCounterCount(CounterType.INDESTRUCTIBLE)).isZero();
        assertThat(gqs.hasKeyword(gd, myojin, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Removing the counter distributes eight +1/+1 counters and grants trample")
    void removingCounterDistributesCountersAndGrantsTrample() {
        Permanent myojin = addReadyMyojin(player1);
        Permanent firstBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondBear = addCreatureReady(player1, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbilityWithDamageAssignments(player1, 0, 0, null,
                Map.of(firstBear.getId(), 3, secondBear.getId(), 5));
        harness.passBothPriorities();

        assertThat(myojin.getCounterCount(CounterType.INDESTRUCTIBLE)).isZero();
        assertThat(gqs.hasKeyword(gd, myojin, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(firstBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(secondBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, firstBear, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, secondBear, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("The ability cannot target an opponent's creature")
    void cannotTargetOpponentsCreature() {
        Permanent myojin = addReadyMyojin(player1);
        Permanent opponentBear = addCreatureReady(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbilityWithDamageAssignments(
                player1, 0, 0, null, Map.of(opponentBear.getId(), 8)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(myojin.getCounterCount(CounterType.INDESTRUCTIBLE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The ability cannot be activated without an indestructible counter")
    void cannotActivateWithoutIndestructibleCounter() {
        Permanent myojin = addReadyMyojin(player1);
        myojin.setCounterCount(CounterType.INDESTRUCTIBLE, 0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough counters");
    }

    @Test
    void rejectsAnIncompleteDistributionBeforeRemovingTheCounter() {
        Permanent myojin = addReadyMyojin(player1);
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbilityWithDamageAssignments(
                player1, 0, 0, null, Map.of(bear.getId(), 7)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sum to 8");
        assertThat(myojin.getCounterCount(CounterType.INDESTRUCTIBLE)).isOne();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void everyChosenCreatureMustReceiveAtLeastOneCounter() {
        Permanent myojin = addReadyMyojin(player1);
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbilityWithDamageAssignments(
                player1, 0, 0, null, Map.of(first.getId(), 0, second.getId(), 8)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("positive");
        assertThat(myojin.getCounterCount(CounterType.INDESTRUCTIBLE)).isOne();
    }

    @Test
    void mayActivateWithoutChoosingAnyCreatures() {
        Permanent myojin = addReadyMyojin(player1);

        harness.activateAbilityWithDamageAssignments(player1, 0, 0, null, Map.of());
        resolveAllTriggers();

        assertThat(myojin.getCounterCount(CounterType.INDESTRUCTIBLE)).isZero();
        assertThat(myojin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void canTargetItselfWhileTappedAndSummoningSick() {
        Permanent myojin = addReadyMyojin(player1);
        myojin.setTapped(true);
        myojin.setSummoningSick(true);

        harness.activateAbilityWithDamageAssignments(player1, 0, 0, null,
                Map.of(myojin.getId(), 8));
        resolveAllTriggers();

        assertThat(myojin.getCounterCount(CounterType.INDESTRUCTIBLE)).isZero();
        assertThat(myojin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(8);
        assertThat(gqs.hasKeyword(gd, myojin, Keyword.TRAMPLE)).isTrue();
        assertThat(myojin.isTapped()).isTrue();
    }

    @Test
    void trampleExpiresButCountersRemainAfterCleanup() {
        Permanent myojin = addReadyMyojin(player1);
        harness.activateAbilityWithDamageAssignments(player1, 0, 0, null,
                Map.of(myojin.getId(), 8));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, myojin, Keyword.TRAMPLE)).isTrue();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, myojin, Keyword.TRAMPLE)).isFalse();
        assertThat(myojin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(8);
    }

    @Test
    void doesNotRedistributeCountersWhenOneTargetChangesController() {
        addReadyMyojin(player1);
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        harness.activateAbilityWithDamageAssignments(player1, 0, 0, null,
                Map.of(first.getId(), 3, second.getId(), 5));

        gd.playerBattlefields.get(player1.getId()).remove(first);
        gd.playerBattlefields.get(player2.getId()).add(first);
        resolveAllTriggers();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, first, Keyword.TRAMPLE)).isFalse();
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, second, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void abilityResolvesAfterItsSourceLeavesTheBattlefield() {
        Permanent myojin = addReadyMyojin(player1);
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        harness.activateAbilityWithDamageAssignments(player1, 0, 0, null,
                Map.of(bear.getId(), 8));

        gd.playerBattlefields.get(player1.getId()).remove(myojin);
        gd.playerGraveyards.get(player1.getId()).add(myojin.getCard());
        resolveAllTriggers();

        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(8);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.TRAMPLE)).isTrue();
    }

    private Permanent addReadyMyojin(Player player) {
        Permanent myojin = addCreatureReady(player, new MyojinOfToweringMight());
        myojin.setCounterCount(CounterType.INDESTRUCTIBLE, 1);
        return myojin;
    }
}
