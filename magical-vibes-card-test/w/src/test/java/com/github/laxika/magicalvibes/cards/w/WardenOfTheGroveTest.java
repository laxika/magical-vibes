package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WardenOfTheGrove.class, GrizzlyBears.class})
class WardenOfTheGroveTest extends BaseCardTest {

    @Test
    @DisplayName("Warden of the Grove puts a +1/+1 counter on itself at the beginning of its controller's end step")
    void growsAtEndStep() {
        Permanent warden = harness.addToBattlefieldAndReturn(player1, new WardenOfTheGrove());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(warden.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Warden of the Grove makes another nontoken creature endure using its counter count")
    void anotherCreatureEnduresWithCounters() {
        Permanent warden = harness.addToBattlefieldAndReturn(player1, new WardenOfTheGrove());
        warden.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent entering = castGrizzlyBears();

        harness.passBothPriorities();
        harness.handleListChoice(player1, "Put 2 +1/+1 counters on this permanent");

        assertThat(entering.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(findPermanents(player1, "Spirit")).isEmpty();
    }

    @Test
    @DisplayName("Warden of the Grove can create a Spirit when another nontoken creature endures")
    void anotherCreatureEnduresWithSpirit() {
        Permanent warden = harness.addToBattlefieldAndReturn(player1, new WardenOfTheGrove());
        warden.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        castGrizzlyBears();

        harness.passBothPriorities();
        harness.handleListChoice(player1, "Create a 2/2 white Spirit creature token");

        Permanent spirit = findPermanents(player1, "Spirit").getFirst();
        assertThat(spirit.getCard().getPower()).isEqualTo(2);
        assertThat(spirit.getCard().getToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Warden of the Grove uses its last-known counters if it leaves before endure resolves")
    void usesLastKnownCountersWhenWardenLeaves() {
        Permanent warden = harness.addToBattlefieldAndReturn(player1, new WardenOfTheGrove());
        warden.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent entering = castGrizzlyBears();

        warden.setMarkedDamage(4);
        harness.runStateBasedActions();
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Put 2 +1/+1 counters on this permanent");

        assertThat(entering.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    private Permanent castGrizzlyBears() {
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        return findPermanent(player1, "Grizzly Bears");
    }

    @Test
    void endureCountsEveryCounterType() {
        Permanent warden = harness.addToBattlefieldAndReturn(player1, new WardenOfTheGrove());
        warden.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        warden.setCounterCount(CounterType.CHARGE, 1);
        Permanent entering = harness.enterBattlefieldAndReturn(player1, new WardenOfTheGrove());

        harness.passBothPriorities();
        harness.handleListChoice(player1, "Put 3 +1/+1 counters on this permanent");

        assertThat(entering.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(warden.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void endureUsesCounterCountAtResolution() {
        Permanent warden = harness.addToBattlefieldAndReturn(player1, new WardenOfTheGrove());
        warden.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent entering = harness.enterBattlefieldAndReturn(player1, new WardenOfTheGrove());
        warden.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        harness.passBothPriorities();
        harness.handleListChoice(player1, "Put 3 +1/+1 counters on this permanent");

        assertThat(entering.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void lastKnownCounterTotalIncludesCountersAddedAfterTriggering() {
        Permanent warden = harness.addToBattlefieldAndReturn(player1, new WardenOfTheGrove());
        warden.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent entering = harness.enterBattlefieldAndReturn(player1, new WardenOfTheGrove());
        warden.setCounterCount(CounterType.CHARGE, 2);
        warden.setMarkedDamage(3);
        harness.runStateBasedActions();

        harness.passBothPriorities();
        harness.handleListChoice(player1, "Put 3 +1/+1 counters on this permanent");

        assertThat(entering.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void createsSpiritWhenEnduringCreatureLeaves() {
        Permanent warden = harness.addToBattlefieldAndReturn(player1, new WardenOfTheGrove());
        warden.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent entering = harness.enterBattlefieldAndReturn(player1, new WardenOfTheGrove());
        entering.setMarkedDamage(2);
        harness.runStateBasedActions();

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Spirit")).hasSize(1);
        Permanent spirit = findPermanents(player1, "Spirit").getFirst();
        assertThat(spirit.getCard().getPower()).isEqualTo(2);
        assertThat(spirit.getCard().getToughness()).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void endureZeroDoesNothing() {
        harness.addToBattlefield(player1, new WardenOfTheGrove());
        Permanent entering = harness.enterBattlefieldAndReturn(player1, new WardenOfTheGrove());

        harness.passBothPriorities();

        assertThat(entering.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(findPermanents(player1, "Spirit")).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotTriggerForItselfOrOpponentsCreatures() {
        Permanent warden = harness.enterBattlefieldAndReturn(player1, new WardenOfTheGrove());
        warden.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.enterBattlefieldAndReturn(player2, new WardenOfTheGrove());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void enduringCreaturesCurrentControllerChoosesAndCreatesSpirit() {
        Permanent warden = harness.addToBattlefieldAndReturn(player1, new WardenOfTheGrove());
        warden.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent entering = harness.enterBattlefieldAndReturn(player1, new WardenOfTheGrove());
        gd.playerBattlefields.get(player1.getId()).remove(entering);
        gd.playerBattlefields.get(player2.getId()).add(entering);

        harness.passBothPriorities();
        harness.handleListChoice(player2, "Create a 2/2 white Spirit creature token");

        assertThat(findPermanents(player1, "Spirit")).isEmpty();
        assertThat(findPermanents(player2, "Spirit")).hasSize(1);
        assertThat(entering.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void spiritTokensDoNotTriggerEndure() {
        Permanent warden = harness.addToBattlefieldAndReturn(player1, new WardenOfTheGrove());
        warden.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.enterBattlefieldAndReturn(player1, new WardenOfTheGrove());

        harness.passBothPriorities();
        harness.handleListChoice(player1, "Create a 2/2 white Spirit creature token");

        assertThat(findPermanents(player1, "Spirit")).hasSize(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void doesNotGrowAtOpponentsEndStep() {
        Permanent warden = harness.addToBattlefieldAndReturn(player1, new WardenOfTheGrove());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.END_STEP);

        assertThat(warden.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
