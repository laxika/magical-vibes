package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.n.NightOfSoulsBetrayal;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EzuriClawOfProgress.class, GrizzlyBears.class, HillGiant.class,
        GloriousAnthem.class, NightOfSoulsBetrayal.class})
class EzuriClawOfProgressTest extends BaseCardTest {

    @Test
    void gainsExperienceForCreatureWithPowerTwoOrLess() {
        harness.addToBattlefield(player1, new EzuriClawOfProgress());

        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.passBothPriorities();

        assertThat(gd.playerExperienceCounters).containsEntry(player1.getId(), 1);
    }

    @Test
    void doesNotGainExperienceForLargerOrOpponentsCreatures() {
        harness.addToBattlefield(player1, new EzuriClawOfProgress());

        harness.enterBattlefieldAndReturn(player1, new HillGiant());
        harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.passBothPriorities();

        assertThat(gd.playerExperienceCounters).doesNotContainKey(player1.getId());
    }

    @Test
    void putsPlusOneCountersEqualToExperienceOnAnotherCreatureAtCombat() {
        harness.addToBattlefield(player1, new EzuriClawOfProgress());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        gd.playerExperienceCounters.put(player1.getId(), 2);

        advanceToBeginningOfCombat(player1);
        harness.handlePermanentChosen(player1, first.getId());
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void cannotTargetEzuriForItsCombatAbility() {
        Permanent ezuri = harness.addToBattlefieldAndReturn(player1, new EzuriClawOfProgress());
        harness.addToBattlefield(player1, new GrizzlyBears());
        gd.playerExperienceCounters.put(player1.getId(), 1);

        advanceToBeginningOfCombat(player1);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ezuri.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void gainsExperienceForSmallCreaturesOnly() {
        harness.addToBattlefield(player1, new EzuriClawOfProgress());

        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.passBothPriorities();
        assertThat(gd.playerExperienceCounters.get(player1.getId())).isEqualTo(1);

        harness.enterBattlefieldAndReturn(player1, new HillGiant());
        harness.passBothPriorities();
        assertThat(gd.playerExperienceCounters.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void putsOneCounterPerExperienceCounterOnAnotherControlledCreatureAtCombat() {
        harness.addToBattlefield(player1, new EzuriClawOfProgress());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        gd.playerExperienceCounters.put(player1.getId(), 3);

        advanceToBeginningOfCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void cannotTargetEzuriWithItsBeginningOfCombatAbility() {
        Permanent ezuri = harness.addToBattlefieldAndReturn(player1, new EzuriClawOfProgress());
        harness.addToBattlefield(player1, new GrizzlyBears());
        gd.playerExperienceCounters.put(player1.getId(), 1);

        advanceToBeginningOfCombat(player1);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ezuri.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void gainsExperienceWhenEzuriItselfEntersWithPowerTwo() {
        harness.addToBattlefield(player1, new NightOfSoulsBetrayal());

        harness.enterBattlefieldAndReturn(player1, new EzuriClawOfProgress());
        harness.passBothPriorities();

        assertThat(gd.playerExperienceCounters).containsEntry(player1.getId(), 1);
    }

    @Test
    void gainsExperienceForCreatureWhosePowerIsReducedToTwoAsItEnters() {
        harness.addToBattlefield(player1, new EzuriClawOfProgress());
        harness.addToBattlefield(player1, new NightOfSoulsBetrayal());

        harness.enterBattlefieldAndReturn(player1, new HillGiant());
        harness.passBothPriorities();

        assertThat(gd.playerExperienceCounters).containsEntry(player1.getId(), 1);
    }

    @Test
    void doesNotGainExperienceForCreatureWhosePowerIsBoostedAboveTwoAsItEnters() {
        harness.addToBattlefield(player1, new EzuriClawOfProgress());
        harness.addToBattlefield(player1, new GloriousAnthem());

        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.passBothPriorities();

        assertThat(gd.playerExperienceCounters.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    void doesNotRecheckEnteringCreaturesPowerWhenExperienceTriggerResolves() {
        harness.addToBattlefield(player1, new EzuriClawOfProgress());
        Permanent entering = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        entering.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.passBothPriorities();

        assertThat(gd.playerExperienceCounters).containsEntry(player1.getId(), 1);
    }

    @Test
    void countsExperienceAtResolutionIncludingNewExperienceGainedInResponse() {
        harness.addToBattlefield(player1, new EzuriClawOfProgress());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        gd.playerExperienceCounters.put(player1.getId(), 1);

        advanceToBeginningOfCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerExperienceCounters).containsEntry(player1.getId(), 2);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void combatAbilityStillTargetsWithZeroExperienceButAddsNoCounters() {
        harness.addToBattlefield(player1, new EzuriClawOfProgress());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new HillGiant());

        advanceToBeginningOfCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void doesNotTriggerAtBeginningOfOpponentsCombat() {
        harness.addToBattlefield(player1, new EzuriClawOfProgress());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        gd.playerExperienceCounters.put(player1.getId(), 3);

        advanceToBeginningOfCombat(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void cannotTargetAnOpponentsCreatureForCombatAbility() {
        harness.addToBattlefield(player1, new EzuriClawOfProgress());
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        gd.playerExperienceCounters.put(player1.getId(), 1);

        advanceToBeginningOfCombat(player1);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponent.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, own.getId());
        harness.passBothPriorities();
        assertThat(own.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void advanceToBeginningOfCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }
}
