package com.github.laxika.magicalvibes.cards.i;

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

@CardUsed({IrohDragonOfTheWest.class, FugitiveWizard.class, GrizzlyBears.class})
class IrohDragonOfTheWestTest extends BaseCardTest {

    @Test
    @DisplayName("Mentor puts a +1/+1 counter on a lesser-power attacking creature")
    void mentorCountersLesserPowerAttacker() {
        Permanent iroh = addCreatureReady(player1, new IrohDragonOfTheWest());
        Permanent lowerPower = addCreatureReady(player1, new FugitiveWizard());
        Permanent equalPower = addCreatureReady(player1, new GrizzlyBears());
        equalPower.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent nonAttacking = addCreatureReady(player1, new GrizzlyBears());

        declareIrohAttackers(List.of(
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

        declareIrohAttackers(List.of(
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

    private void declareIrohAttackers(List<Integer> attackers) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, attackers);
    }
}
