package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.d.DressDown;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IrohDragonOfTheWest.class, FugitiveWizard.class, GrizzlyBears.class, DressDown.class})
class IrohDragonOfTheWestTest extends BaseCardTest {

    @Test
    @DisplayName("Mentor puts a +1/+1 counter on a lesser-power attacking creature")
    void mentorCountersLesserPowerAttacker() {
        Permanent iroh = addCreatureReady(player1, new IrohDragonOfTheWest());
        Permanent lowerPower = addCreatureReady(player1, new FugitiveWizard());
        Permanent equalPower = addCreatureReady(player1, new GrizzlyBears());
        equalPower.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent nonAttacking = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(iroh),
                gd.playerBattlefields.get(player1.getId()).indexOf(lowerPower),
                gd.playerBattlefields.get(player1.getId()).indexOf(equalPower)));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(lowerPower.getId());

        harness.handlePermanentChosen(player1, lowerPower.getId());
        harness.passBothPriorities();

        assertThat(lowerPower.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
        assertThat(equalPower.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(nonAttacking.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Beginning of combat gives countered creatures firebending 2 until end of turn")
    void counteredCreaturesGainFirebendingUntilEndOfTurn() {
        addCreatureReady(player1, new IrohDragonOfTheWest());
        Permanent countered = addCreatureReady(player1, new GrizzlyBears());
        countered.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent withoutCounter = addCreatureReady(player1, new FugitiveWizard());

        advanceToBeginningOfCombat();

        assertThat(gqs.hasKeyword(gd, countered, Keyword.FIREBENDING)).isTrue();
        assertThat(gqs.hasKeyword(gd, withoutCounter, Keyword.FIREBENDING)).isFalse();

        declareAttackers(List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(countered),
                gd.playerBattlefields.get(player1.getId()).indexOf(withoutCounter)));
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);

        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    private void advanceToBeginningOfCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    void mentorDoesNotCounterTargetWhosePowerBecomesEqualBeforeResolution() {
        addCreatureReady(player1, new IrohDragonOfTheWest());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, creature.getId());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void mentorCounterDoesNotGrantFirebendingDuringSameCombat() {
        addCreatureReady(player1, new IrohDragonOfTheWest());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        advanceToBeginningOfCombat();
        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIREBENDING)).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    void nonPowerCounterQualifiesAndOpponentCreaturesAreExcluded() {
        Permanent iroh = addCreatureReady(player1, new IrohDragonOfTheWest());
        iroh.setCounterCount(CounterType.CHARGE, 1);
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        opponentCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        advanceToBeginningOfCombat();

        assertThat(gqs.hasKeyword(gd, iroh, Keyword.FIREBENDING)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.FIREBENDING)).isFalse();
        declareAttackers(List.of(0));
        harness.passUntil(TurnStep.END_OF_COMBAT);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
    }

    @Test
    void gainingCounterAfterCombatTriggerResolvesDoesNotGrantFirebending() {
        addCreatureReady(player1, new IrohDragonOfTheWest());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        advanceToBeginningOfCombat();
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIREBENDING)).isFalse();
        declareAttackers(List.of(1));
        harness.passUntil(TurnStep.END_OF_COMBAT);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    void losingLastCounterAfterGrantDoesNotRemoveFirebending() {
        addCreatureReady(player1, new IrohDragonOfTheWest());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        advanceToBeginningOfCombat();
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIREBENDING)).isTrue();
        declareAttackers(List.of(1));
        harness.passUntil(TurnStep.END_OF_COMBAT);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
    }

    @Test
    void grantedFirebendingExpiresAtEndOfTurn() {
        addCreatureReady(player1, new IrohDragonOfTheWest());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        advanceToBeginningOfCombat();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIREBENDING)).isTrue();
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIREBENDING)).isFalse();

        declareAttackers(List.of(1));
        harness.passUntil(TurnStep.END_OF_COMBAT);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @CardUsed(DressDown.class)
    void losingAbilitiesAfterGrantPreventsFirebendingTrigger() {
        addCreatureReady(player1, new IrohDragonOfTheWest());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        advanceToBeginningOfCombat();
        harness.setHand(player1, List.of(new DressDown()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIREBENDING)).isFalse();
        declareAttackers(List.of(1));
        harness.passUntil(TurnStep.END_OF_COMBAT);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }
}
