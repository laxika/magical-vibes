package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.b.BasalThrull;
import com.github.laxika.magicalvibes.cards.r.RiverMerfolk;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EbonPraetor.class, BasalThrull.class, RiverMerfolk.class})
class EbonPraetorTest extends BaseCardTest {

    @Test
    @DisplayName("Its upkeep trigger puts a -2/-2 counter on it")
    void upkeepPutsMinusTwoMinusTwoCounter() {
        Permanent praetor = addCreatureReady(player1, new EbonPraetor());

        beginUpkeep();

        assertThat(praetor.getCounterCount(CounterType.MINUS_TWO_MINUS_TWO)).isEqualTo(1);
    }

    @Test
    @DisplayName("Its upkeep trigger does not fire during an opponent's upkeep")
    void upkeepTriggerOnlyFiresDuringItsControllersUpkeep() {
        Permanent praetor = addCreatureReady(player1, new EbonPraetor());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(praetor.getCounterCount(CounterType.MINUS_TWO_MINUS_TWO)).isZero();
    }

    @Test
    @DisplayName("Sacrificing a non-Thrull removes a -2/-2 counter without adding a power counter")
    void sacrificingNonThrullRemovesCounter() {
        Permanent praetor = addCreatureReady(player1, new EbonPraetor());
        Permanent merfolk = addCreatureReady(player1, new RiverMerfolk());

        beginUpkeep();
        activateSacrificeAbility(merfolk);

        assertThat(praetor.getCounterCount(CounterType.MINUS_TWO_MINUS_TWO)).isZero();
        assertThat(praetor.getCounterCount(CounterType.PLUS_ONE_PLUS_ZERO)).isZero();
        harness.assertInGraveyard(player1, "River Merfolk");
    }

    @Test
    @DisplayName("Sacrificing a Thrull adds a +1/+0 counter")
    void sacrificingThrullAddsPowerCounter() {
        Permanent praetor = addCreatureReady(player1, new EbonPraetor());
        Permanent thrull = addCreatureReady(player1, new BasalThrull());

        beginUpkeep();
        activateSacrificeAbility(thrull);

        assertThat(praetor.getCounterCount(CounterType.MINUS_TWO_MINUS_TWO)).isZero();
        assertThat(praetor.getCounterCount(CounterType.PLUS_ONE_PLUS_ZERO)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Basal Thrull");
    }

    @Test
    @DisplayName("Sacrificing a Thrull still adds a +1/+0 counter when there is no -2/-2 counter to remove")
    void sacrificingThrullWithoutMinusCounterStillAddsPowerCounter() {
        Permanent praetor = addCreatureReady(player1, new EbonPraetor());
        Permanent thrull = addCreatureReady(player1, new BasalThrull());

        beginUpkeep();
        praetor.setCounterCount(CounterType.MINUS_TWO_MINUS_TWO, 0);
        activateSacrificeAbility(thrull);

        assertThat(praetor.getCounterCount(CounterType.MINUS_TWO_MINUS_TWO)).isZero();
        assertThat(praetor.getCounterCount(CounterType.PLUS_ONE_PLUS_ZERO)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Basal Thrull");
    }

    @Test
    @DisplayName("The sacrifice ability can be activated only once each turn and only during upkeep")
    void activationTimingAndFrequencyAreRestricted() {
        addCreatureReady(player1, new EbonPraetor());
        Permanent firstFodder = addCreatureReady(player1, new RiverMerfolk());
        beginUpkeep();
        activateSacrificeAbility(firstFodder);

        addCreatureReady(player1, new RiverMerfolk());
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The sacrifice ability cannot be activated during an opponent's upkeep")
    void cannotActivateDuringOpponentsUpkeep() {
        addCreatureReady(player1, new EbonPraetor());
        addCreatureReady(player1, new RiverMerfolk());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("your upkeep");
    }

    private void activateSacrificeAbility(Permanent sacrificed) {
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handlePermanentChosen(player1, sacrificed.getId());
        harness.passBothPriorities();
    }

    private void beginUpkeep() {
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
    }
}
