package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BaboonSpirit;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TuiAndLaMoonAndOcean.class, BaboonSpirit.class})
class TuiAndLaMoonAndOceanTest extends BaseCardTest {

    @Test
    @DisplayName("Becoming tapped draws a card")
    void becomingTappedDrawsCard() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new TuiAndLaMoonAndOcean());
        harness.setLibrary(player1, List.of(new BaboonSpirit()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        source.tap();
        harness.inMutationScope(
                () -> harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, source));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Becoming untapped puts a +1/+1 counter on it")
    void becomingUntappedPutsCounterOnSource() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new TuiAndLaMoonAndOcean());
        source.setSummoningSick(false);
        source.tap();

        advanceToUntapStep();
        harness.passBothPriorities();

        assertThat(source.getCounters().getOrDefault(CounterType.PLUS_ONE_PLUS_ONE, 0)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tapping another permanent does not draw a card")
    void anotherPermanentBecomingTappedDoesNotDraw() {
        harness.addToBattlefield(player1, new TuiAndLaMoonAndOcean());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new BaboonSpirit());
        harness.setLibrary(player1, List.of(new BaboonSpirit()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        other.tap();
        harness.inMutationScope(
                () -> harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, other));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("An already untapped source does not get a counter during untap")
    void alreadyUntappedSourceDoesNotGetCounter() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new TuiAndLaMoonAndOcean());

        advanceToUntapStep();

        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Each actual untap gives another counter")
    void repeatedUntapsGiveSeparateCounters() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new TuiAndLaMoonAndOcean());
        source.tap();
        harness.performUntapStep(player1);
        harness.passBothPriorities();
        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        source.tap();
        harness.performUntapStep(player1);
        harness.passBothPriorities();

        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Attacking draws a card when the source taps")
    void attackingDrawsCard() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new TuiAndLaMoonAndOcean());
        source.setSummoningSick(false);
        harness.setLibrary(player1, List.of(new BaboonSpirit()));
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0));
        harness.passBothPriorities();

        assertThat(source.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    private void advanceToUntapStep() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player1, TurnStep.UPKEEP);
    }
}
