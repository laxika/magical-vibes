package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EzuriClawOfProgress.class, GrizzlyBears.class, HillGiant.class})
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

    private void advanceToBeginningOfCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
