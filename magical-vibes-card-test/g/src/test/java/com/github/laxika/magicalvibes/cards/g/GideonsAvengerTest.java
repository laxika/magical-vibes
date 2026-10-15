package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GideonsAvenger.class, RuneclawBear.class, Island.class, GideonsLawkeeper.class})
class GideonsAvengerTest extends BaseCardTest {

    // "Whenever a creature an opponent controls becomes tapped, put a +1/+1 counter on this creature."

    @Test
    @DisplayName("An opponent's creature becoming tapped puts a +1/+1 counter on Gideon's Avenger")
    void opponentCreatureTapAddsCounter() {
        Permanent avenger = harness.addToBattlefieldAndReturn(player1, new GideonsAvenger());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());

        tap(bears);
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(avenger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Each opponent creature tap adds another counter")
    void countersAccumulate() {
        Permanent avenger = harness.addToBattlefieldAndReturn(player1, new GideonsAvenger());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());

        tap(first);
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
        tap(second);
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(avenger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Tapping your own creature does not trigger")
    void ownCreatureTapDoesNotTrigger() {
        Permanent avenger = harness.addToBattlefieldAndReturn(player1, new GideonsAvenger());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());

        tap(bears);

        assertThat(gd.stack).isEmpty();
        assertThat(avenger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Tapping an opponent's noncreature permanent does not trigger")
    void opponentLandTapDoesNotTrigger() {
        Permanent avenger = harness.addToBattlefieldAndReturn(player1, new GideonsAvenger());
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());

        tap(island);

        assertThat(gd.stack).isEmpty();
        assertThat(avenger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void triggerDoesNotPutCounterOnAvengerThatLeftAndReturned() {
        Permanent original = harness.addToBattlefieldAndReturn(player1, new GideonsAvenger());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        tap(target);
        gd.playerBattlefields.get(player1.getId()).remove(original);
        Permanent returned = harness.addToBattlefieldAndReturn(player1, original.getCard());

        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void eachAvengerGetsItsOwnCounter() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GideonsAvenger());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GideonsAvenger());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());

        tap(target);
        assertThat(gd.stack).hasSize(2);
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
    @Test
    void opponentTapCostAndForcedTapEachTrigger() {
        Permanent avenger = harness.addToBattlefieldAndReturn(player1, new GideonsAvenger());
        addCreatureReady(player2, new GideonsLawkeeper());
        Permanent target = addCreatureReady(player2, new RuneclawBear());
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.activateAbility(player2, 0, null, target.getId());
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
        assertThat(avenger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
        assertThat(target.isTapped()).isTrue();
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(avenger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void tappingAlreadyTappedCreatureDoesNotTriggerAgain() {
        Permanent avenger = harness.addToBattlefieldAndReturn(player1, new GideonsAvenger());
        addCreatureReady(player1, new GideonsLawkeeper());
        Permanent target = addCreatureReady(player2, new RuneclawBear());
        target.tap();
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 1, null, target.getId());
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(gd.stack).isEmpty();
        assertThat(avenger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
    private void tap(Permanent permanent) {
        permanent.tap();
        harness.inMutationScope(
                () -> harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, permanent));
    }
}
