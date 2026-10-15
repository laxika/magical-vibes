package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KuldothaCackler.class, Mountain.class})
class KuldothaCacklerTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+0 for each permanent you control with an oil counter")
    void scalesWithControlledOilPermanents() {
        Permanent cackler = addCreatureReady(player1, new KuldothaCackler());
        addOilPermanent(player1);
        addOilPermanent(player1);
        addOilPermanent(player2);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(cackler.getPowerModifier()).isEqualTo(2);
        assertThat(cackler.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Counts the Cackler itself when it has an oil counter")
    void countsSourcePermanent() {
        Permanent cackler = addCreatureReady(player1, new KuldothaCackler());
        cackler.setCounterCount(CounterType.OIL, 1);
        addOilPermanent(player1);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(cackler.getPowerModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("The attack boost wears off at end of turn")
    void boostExpiresAtEndOfTurn() {
        Permanent cackler = addCreatureReady(player1, new KuldothaCackler());
        addOilPermanent(player1);

        declareAttackers(List.of(0));
        resolveAllTriggers();
        assertThat(cackler.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(cackler.getPowerModifier()).isZero();
    }

    @Test
    void countsPermanentsRatherThanCountersAndIgnoresOtherCounterTypes() {
        Permanent cackler = addCreatureReady(player1, new KuldothaCackler());
        Permanent oilLand = harness.addToBattlefieldAndReturn(player1, new Mountain());
        oilLand.setCounterCount(CounterType.OIL, 5);
        Permanent otherLand = harness.addToBattlefieldAndReturn(player1, new Mountain());
        otherLand.setCounterCount(CounterType.CHARGE, 2);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(cackler.getPowerModifier()).isEqualTo(1);
        assertThat(cackler.getToughnessModifier()).isZero();
    }

    @Test
    void countsOilPermanentsAtResolutionAndDoesNotUpdateBoostAfterward() {
        Permanent cackler = addCreatureReady(player1, new KuldothaCackler());

        declareAttackers(List.of(0));
        cackler.setCounterCount(CounterType.OIL, 1);
        addOilPermanent(player1);
        resolveAllTriggers();

        assertThat(cackler.getPowerModifier()).isEqualTo(2);
        cackler.setCounterCount(CounterType.OIL, 0);
        addOilPermanent(player1);
        addOilPermanent(player1);
        assertThat(cackler.getPowerModifier()).isEqualTo(2);
    }

    @Test
    void getsNoBoostWhenLastOilCounterIsRemovedBeforeResolution() {
        Permanent cackler = addCreatureReady(player1, new KuldothaCackler());
        cackler.setCounterCount(CounterType.OIL, 1);

        declareAttackers(List.of(0));
        cackler.setCounterCount(CounterType.OIL, 0);
        resolveAllTriggers();

        assertThat(cackler.getPowerModifier()).isZero();
        assertThat(cackler.getToughnessModifier()).isZero();
    }

    private void addOilPermanent(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new KuldothaCackler());
        permanent.setCounterCount(CounterType.OIL, 1);
    }
}
