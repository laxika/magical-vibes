package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.cards.s.Solemnity;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheOzolith.class, GrizzlyBears.class, Unsummon.class, Solemnity.class})
class TheOzolithTest extends BaseCardTest {

    @Test
    void storesAllCountersFromAControlledCreatureThatLeaves() {
        Permanent ozolith = harness.addToBattlefieldAndReturn(player1, new TheOzolith());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        creature.setCounterCount(CounterType.CHARGE, 3);

        unsummon(player1, creature);

        assertThat(ozolith.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(ozolith.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    @Test
    void doesNotStoreCountersFromAnOpponentsCreature() {
        Permanent ozolith = harness.addToBattlefieldAndReturn(player1, new TheOzolith());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        unsummon(player1, creature);

        assertThat(ozolith.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void movesAllCountersOntoTheChosenCreatureAtBeginningOfCombat() {
        Permanent ozolith = harness.addToBattlefieldAndReturn(player1, new TheOzolith());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        ozolith.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        ozolith.setCounterCount(CounterType.CHARGE, 3);

        advanceToCombat(player1);
        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(
                PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(creature.getId());

        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(ozolith.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(ozolith.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(creature.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    @Test
    void decliningTheMoveLeavesTheCountersOnTheOzolith() {
        Permanent ozolith = harness.addToBattlefieldAndReturn(player1, new TheOzolith());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        ozolith.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(ozolith.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void doesNotTriggerAtBeginningOfCombatWithoutCounters() {
        harness.addToBattlefield(player1, new TheOzolith());
        harness.addToBattlefield(player1, new GrizzlyBears());

        advanceToCombat(player1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotMoveCountersWhenTheTargetCannotReceiveThem() {
        Permanent ozolith = harness.addToBattlefieldAndReturn(player1, new TheOzolith());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        ozolith.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        ozolith.setCounterCount(CounterType.CHARGE, 3);
        harness.addToBattlefield(player2, new Solemnity());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(ozolith.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(ozolith.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
        assertThat(creature.getCounters()).isEmpty();
    }

    @Test
    void canMoveCountersOntoAnOpponentsCreature() {
        Permanent ozolith = harness.addToBattlefieldAndReturn(player1, new TheOzolith());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        ozolith.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        ozolith.setCounterCount(CounterType.FLYING, 1);

        advanceToCombat(player1);
        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(
                PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(creature.getId());
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(ozolith.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(ozolith.getCounterCount(CounterType.FLYING)).isZero();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(creature.getCounterCount(CounterType.FLYING)).isEqualTo(1);
    }

    @Test
    void keepsCountersWhenTheTargetLeavesBeforeResolution() {
        Permanent ozolith = harness.addToBattlefieldAndReturn(player1, new TheOzolith());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        ozolith.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, creature.getId());
        unsummon(player1, creature);

        assertThat(ozolith.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void doesNotTriggerDuringAnOpponentsCombat() {
        Permanent ozolith = harness.addToBattlefieldAndReturn(player1, new TheOzolith());
        harness.addToBattlefield(player1, new GrizzlyBears());
        ozolith.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        advanceToCombat(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(ozolith.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void addsLeavingCreaturesKeywordCountersToExistingCounters() {
        Permanent ozolith = harness.addToBattlefieldAndReturn(player1, new TheOzolith());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        ozolith.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        ozolith.setCounterCount(CounterType.FLYING, 1);
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        creature.setCounterCount(CounterType.FLYING, 1);

        unsummon(player1, creature);

        assertThat(ozolith.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        assertThat(ozolith.getCounterCount(CounterType.FLYING)).isEqualTo(2);
    }

    @Test
    void doesNotTriggerWhenACounterlessCreatureLeaves() {
        Permanent ozolith = harness.addToBattlefieldAndReturn(player1, new TheOzolith());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(ozolith.getCounters()).isEmpty();
    }

    private void unsummon(Player caster, Permanent target) {
        harness.setHand(caster, List.of(new Unsummon()));
        harness.addMana(caster, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(caster, 0, target.getId());
        harness.passBothPriorities();
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }
}
