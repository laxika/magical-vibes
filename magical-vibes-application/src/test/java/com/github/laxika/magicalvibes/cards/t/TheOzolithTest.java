package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
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

@CardUsed({TheOzolith.class, GrizzlyBears.class, Unsummon.class})
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

    private void unsummon(Player caster, Permanent target) {
        harness.setHand(caster, List.of(new Unsummon()));
        harness.addMana(caster, ManaColor.BLUE, 1);
        harness.castInstant(caster, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
