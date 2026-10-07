package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HardenedScales;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TesterOfTheTangential.class, GrizzlyBears.class, TrollAscetic.class, HardenedScales.class})
class TesterOfTheTangentialTest extends BaseCardTest {

    @Test
    void paysXThenMovesCountersToAnotherCreature() {

        Permanent tester = harness.addToBattlefieldAndReturn(player1, new TesterOfTheTangential());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        tester.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        advanceToCombat(player1);
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class)).isNotNull();
        harness.handleXValueChosen(player1, 2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(tester.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void payingZeroStillCreatesTheReflexiveTargetedAbility() {

        Permanent tester = harness.addToBattlefieldAndReturn(player1, new TesterOfTheTangential());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        tester.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        advanceToCombat(player1);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(tester.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class)).isNull();
    }

    @Test
    void cannotChooseTheSourceAsAnotherCreature() {

        Permanent tester = harness.addToBattlefieldAndReturn(player1, new TesterOfTheTangential());
        tester.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        advanceToCombat(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 1);

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNull();
        assertThat(tester.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void incrementAddsACounterWhenMoreManaThanPowerAndToughnessIsSpent() {
        Permanent tester = harness.addToBattlefieldAndReturn(player1, new TesterOfTheTangential());

        harness.castFromHand(player1, new TesterOfTheTangential(), "{1}{U}");
        harness.passBothPriorities();

        assertThat(tester.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void incrementDoesNotTriggerWhenManaSpentEqualsPowerAndToughness() {
        Permanent tester = harness.addToBattlefieldAndReturn(player1, new TesterOfTheTangential());
        tester.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.castFromHand(player1, new TesterOfTheTangential(), "{1}{U}");
        harness.passBothPriorities();

        assertThat(tester.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void incrementRechecksPowerAndToughnessWhenTheTriggerResolves() {
        Permanent tester = harness.addToBattlefieldAndReturn(player1, new TesterOfTheTangential());

        harness.castFromHand(player1, new TesterOfTheTangential(), "{1}{U}");
        tester.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        assertThat(tester.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void incrementDoesNotTriggerForAnOpponentsSpell() {
        Permanent tester = harness.addToBattlefieldAndReturn(player1, new TesterOfTheTangential());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player2, new TesterOfTheTangential(), "{1}{U}");
        harness.passBothPriorities();

        assertThat(tester.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void combatAbilityDoesNotTriggerOnTheOpponentsTurn() {
        Permanent tester = harness.addToBattlefieldAndReturn(player1, new TesterOfTheTangential());
        tester.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        advanceToCombat(player2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class)).isNull();
        assertThat(tester.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void mayChooseToPayZeroEvenWithoutMana() {
        harness.addToBattlefield(player1, new TesterOfTheTangential());
        harness.addToBattlefield(player2, new GrizzlyBears());

        advanceToCombat(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class)).isNotNull();
    }

    @Test
    void payingMoreThanTheAvailableCountersMovesOnlyThoseAvailable() {
        Permanent tester = harness.addToBattlefieldAndReturn(player1, new TesterOfTheTangential());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        tester.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        advanceToCombat(player1);
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 3);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(tester.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void reflexiveAbilityCannotTargetAnOpponentsHexproofCreature() {
        Permanent tester = harness.addToBattlefieldAndReturn(player1, new TesterOfTheTangential());
        Permanent legalTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent hexproofTarget = harness.addToBattlefieldAndReturn(player2, new TrollAscetic());
        tester.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        advanceToCombat(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 1);

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(legalTarget.getId()).doesNotContain(hexproofTarget.getId(), tester.getId());
    }

    @Test
    void movesCountersAsOneEventForHardenedScales() {
        Permanent tester = harness.addToBattlefieldAndReturn(player1, new TesterOfTheTangential());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new HardenedScales());
        tester.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        advanceToCombat(player1);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 2);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(tester.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }
}
