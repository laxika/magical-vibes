package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MaesterSeymour.class, GrizzlyBears.class})
class MaesterSeymourTest extends BaseCardTest {

    @Test
    void putsCountersEqualToItsPowerOnAnotherControlledCreatureAtCombat() {
        Permanent maester = harness.addToBattlefieldAndReturn(player1, new MaesterSeymour());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        advanceToBeginningOfCombat(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(maester.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void monstrosityUsesAllCountersAmongControlledCreatures() {
        Permanent maester = harness.addToBattlefieldAndReturn(player1, new MaesterSeymour());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        first.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        second.setCounterCount(CounterType.CHARGE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(maester.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(maester.isMonstrous()).isTrue();
    }

    @Test
    void cannotTargetMaesterForItsCombatAbility() {
        Permanent maester = harness.addToBattlefieldAndReturn(player1, new MaesterSeymour());
        harness.addToBattlefield(player1, new GrizzlyBears());

        advanceToBeginningOfCombat(player1);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, maester.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canActivateMonstrosityAgainAfterBecomingMonstrous() {
        Permanent maester = harness.addToBattlefieldAndReturn(player1, new MaesterSeymour());
        maester.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(maester.isMonstrous()).isTrue();
        assertThat(maester.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(maester.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(maester.isMonstrous()).isTrue();
    }

    @Test
    void monstrosityCountsItsOwnCountersButNotOpponentsAndUsesResolutionTimeCount() {
        Permanent maester = harness.addToBattlefieldAndReturn(player1, new MaesterSeymour());
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        maester.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        opponent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 10);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        ally.setCounterCount(CounterType.CHARGE, 3);
        harness.passBothPriorities();

        assertThat(maester.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(7);
        assertThat(maester.isMonstrous()).isTrue();
    }

    @Test
    void becomesMonstrousEvenWhenThereAreNoCounters() {
        Permanent maester = harness.addToBattlefieldAndReturn(player1, new MaesterSeymour());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(maester.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(maester.isMonstrous()).isTrue();
    }

    @Test
    void combatTriggerUsesPowerAtResolution() {
        Permanent maester = harness.addToBattlefieldAndReturn(player1, new MaesterSeymour());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToBeginningOfCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        maester.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    void combatAbilityDoesNotTriggerOnOpponentsTurn() {
        harness.addToBattlefield(player1, new MaesterSeymour());
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToBeginningOfCombat(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(ally.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void advanceToBeginningOfCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }
}
