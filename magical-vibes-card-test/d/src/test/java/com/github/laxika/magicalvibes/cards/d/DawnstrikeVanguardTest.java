package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BeamsawProspector;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DawnstrikeVanguard.class, BeamsawProspector.class})
class DawnstrikeVanguardTest extends BaseCardTest {

    @Test
    void putsCountersOnOtherCreaturesWhenTwoOrMoreControlledCreaturesAreTapped() {
        Permanent vanguard = harness.addToBattlefieldAndReturn(player1, new DawnstrikeVanguard());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new BeamsawProspector());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new BeamsawProspector());
        first.tap();
        second.tap();

        resolveEndStep();

        assertThat(vanguard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotTriggerWithFewerThanTwoTappedCreatures() {
        Permanent vanguard = harness.addToBattlefieldAndReturn(player1, new DawnstrikeVanguard());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new BeamsawProspector());
        bear.tap();

        resolveEndStep();

        assertThat(vanguard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void countsTheVanguardAsTappedButDoesNotPutACounterOnItself() {
        Permanent vanguard = harness.addToBattlefieldAndReturn(player1, new DawnstrikeVanguard());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new BeamsawProspector());
        vanguard.tap();
        bear.tap();

        resolveEndStep();

        assertThat(vanguard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void alsoCountersUntappedCreaturesButNotOpposingCreatures() {
        Permanent vanguard = harness.addToBattlefieldAndReturn(player1, new DawnstrikeVanguard());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new BeamsawProspector());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new BeamsawProspector());
        Permanent untapped = harness.addToBattlefieldAndReturn(player1, new BeamsawProspector());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new BeamsawProspector());
        first.tap();
        second.tap();
        opponent.tap();

        resolveEndStep();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(untapped.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(vanguard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void opposingTappedCreaturesDoNotSatisfyTheCondition() {
        Permanent vanguard = harness.addToBattlefieldAndReturn(player1, new DawnstrikeVanguard());
        Permanent own = harness.addToBattlefieldAndReturn(player1, new BeamsawProspector());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new BeamsawProspector());
        own.tap();
        opponent.tap();

        beginEndStep();

        assertThat(gd.stack).isEmpty();
        assertThat(own.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(vanguard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void conditionIsCheckedAgainWhenTheAbilityResolves() {
        harness.addToBattlefield(player1, new DawnstrikeVanguard());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new BeamsawProspector());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new BeamsawProspector());
        first.tap();
        second.tap();

        beginEndStep();
        assertThat(gd.stack).hasSize(1);
        second.untap();
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void recipientsAreDeterminedWhenTheAbilityResolves() {
        harness.addToBattlefield(player1, new DawnstrikeVanguard());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new BeamsawProspector());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new BeamsawProspector());
        first.tap();
        second.tap();

        beginEndStep();
        Permanent newcomer = harness.addToBattlefieldAndReturn(player1, new BeamsawProspector());
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(newcomer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotTriggerDuringOpponentsEndStep() {
        Permanent vanguard = harness.addToBattlefieldAndReturn(player1, new DawnstrikeVanguard());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new BeamsawProspector());
        vanguard.tap();
        other.tap();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(other.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void lifelinkGainsLifeFromCombatDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent vanguard = addCreatureReady(player1, new DawnstrikeVanguard());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(vanguard)));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    private void beginEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
    }

    private void resolveEndStep() {
        beginEndStep();
        harness.passBothPriorities();
    }
}
