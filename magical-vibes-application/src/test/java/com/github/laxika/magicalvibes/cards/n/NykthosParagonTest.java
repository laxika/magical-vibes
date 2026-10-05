package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NykthosParagon.class, GrizzlyBears.class})
class NykthosParagonTest extends BaseCardTest {

    @Test
    @DisplayName("Gaining life may put that many counters on each creature you control")
    void gainingLifePutsCountersOnEachControlledCreature() {
        Permanent paragon = harness.addToBattlefieldAndReturn(player1, new NykthosParagon());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setLife(player1, 20);
        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 3));
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(paragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(opponentBears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The life-gain ability puts counters only once each turn")
    void putsCountersOnlyOnceEachTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new NykthosParagon());
        harness.setLife(player1, 20);

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 2));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 4));
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    @Test
    @DisplayName("Declining the optional counter placement puts no counters")
    void decliningDoesNotPutCounters() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new NykthosParagon());
        harness.setLife(player1, 20);

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 3));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 2));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Accepting counter placement prevents further triggers that turn")
    void acceptingPreventsFurtherLifeGainTriggers() {
        Permanent paragon = harness.addToBattlefieldAndReturn(player1, new NykthosParagon());

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 2));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 4));

        assertThat(gd.stack).isEmpty();
        assertThat(paragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Only one queued instance may put counters on creatures")
    void acceptingOneQueuedInstanceMakesOtherInstancesDoNothing() {
        Permanent paragon = harness.addToBattlefieldAndReturn(player1, new NykthosParagon());

        harness.inMutationScope(() -> {
            harness.getLifeSupport().applyGainLife(gd, player1.getId(), 2);
            harness.getLifeSupport().applyGainLife(gd, player1.getId(), 5);
        });
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(paragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Declining a queued instance allows an earlier instance to resolve")
    void decliningQueuedInstanceAllowsEarlierLifeGainAmount() {
        Permanent paragon = harness.addToBattlefieldAndReturn(player1, new NykthosParagon());

        harness.inMutationScope(() -> {
            harness.getLifeSupport().applyGainLife(gd, player1.getId(), 5);
            harness.getLifeSupport().applyGainLife(gd, player1.getId(), 2);
        });
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(paragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Each Paragon can put counters once independently")
    void multipleParagonsHaveIndependentUses() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new NykthosParagon());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new NykthosParagon());

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 3));
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
    }

    @Test
    @DisplayName("Opponent life gain and gaining zero life do not trigger Paragon")
    void opponentLifeGainAndZeroLifeDoNotTrigger() {
        Permanent paragon = harness.addToBattlefieldAndReturn(player1, new NykthosParagon());

        harness.inMutationScope(() -> {
            harness.getLifeSupport().applyGainLife(gd, player2.getId(), 3);
            harness.getLifeSupport().applyGainLife(gd, player1.getId(), 0);
        });

        assertThat(gd.stack).isEmpty();
        assertThat(paragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Paragon can put counters again during the opponent's next turn")
    void counterPlacementResetsOnNextTurn() {
        Permanent paragon = harness.addToBattlefieldAndReturn(player1, new NykthosParagon());
        harness.forceActivePlayer(player1);

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 2));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 3));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(paragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @Test
    @DisplayName("The trigger survives its source leaving and includes creatures present at resolution")
    void triggerSurvivesSourceLeavingAndUsesCurrentCreatures() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new NykthosParagon());
        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 3));
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, source));
        Permanent newcomer = harness.addToBattlefieldAndReturn(player1, new NykthosParagon());

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(newcomer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gd.findExiledCard(source.getCard().getId())).isNotNull();
    }
}
