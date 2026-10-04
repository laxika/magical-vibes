package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoliathTruck.class, GrizzlyBears.class})
class GoliathTruckTest extends BaseCardTest {

    @Test
    void stowagePutsTwoCountersOnAnotherAttackingCreature() {
        Permanent truck = addCreatureReady(player1, new GoliathTruck());
        Permanent crew = addCreatureReady(player1, new GrizzlyBears());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, crew.getId());
        harness.passBothPriorities();

        declareAttackers(player1, List.of(0, 2));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).containsExactly(attacker.getId());
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();

        assertThat(truck.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(crew.isTapped()).isTrue();
        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void stowageCannotTargetTheTruckOrANonattackingCreature() {
        Permanent truck = addCreatureReady(player1, new GoliathTruck());
        Permanent crew = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, crew.getId());
        harness.passBothPriorities();
        declareAttackers(player1, List.of(0, 2));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, truck.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, crew.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void attackingAloneDoesNotPutCountersOnTheTruck() {
        Permanent truck = addCreatureReady(player1, new GoliathTruck());
        Permanent crew = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, crew.getId());
        harness.passBothPriorities();

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(truck.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(crew.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void stowageDoesNotPutCountersOnATargetThatLeavesCombat() {
        addCreatureReady(player1, new GoliathTruck());
        Permanent crew = addCreatureReady(player1, new GrizzlyBears());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, crew.getId());
        harness.passBothPriorities();
        declareAttackers(List.of(0, 2));
        harness.handlePermanentChosen(player1, attacker.getId());

        attacker.setAttacking(false);
        harness.passBothPriorities();

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void stowageResolvesAfterTheTruckLeavesTheBattlefield() {
        Permanent truck = addCreatureReady(player1, new GoliathTruck());
        Permanent crew = addCreatureReady(player1, new GrizzlyBears());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, crew.getId());
        harness.passBothPriorities();
        declareAttackers(List.of(0, 2));
        harness.handlePermanentChosen(player1, attacker.getId());

        gd.playerBattlefields.get(player1.getId()).remove(truck);
        gd.playerGraveyards.get(player1.getId()).add(truck.getCard());
        harness.passBothPriorities();

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }
    @Test
    void summoningSickCreatureCanCrewTheTruck() {
        Permanent truck = addCreatureReady(player1, new GoliathTruck());
        Permanent crew = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        crew.setSummoningSick(true);
        addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, crew.getId());
        harness.passBothPriorities();

        assertThat(crew.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, truck)).isTrue();
    }
}
