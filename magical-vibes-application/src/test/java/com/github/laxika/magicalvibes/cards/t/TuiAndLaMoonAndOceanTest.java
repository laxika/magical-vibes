package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TuiAndLaMoonAndOcean.class, GrizzlyBears.class})
class TuiAndLaMoonAndOceanTest extends BaseCardTest {

    @Test
    @DisplayName("Becoming tapped draws a card")
    void becomingTappedDrawsCard() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new TuiAndLaMoonAndOcean());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
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
        Permanent other = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        other.tap();
        harness.inMutationScope(
                () -> harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, other));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    private void advanceToUntapStep() {
        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
