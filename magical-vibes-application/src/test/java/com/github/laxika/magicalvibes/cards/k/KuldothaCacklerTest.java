package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KuldothaCacklerTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+0 for each permanent you control with an oil counter")
    void scalesWithControlledOilPermanents() {
        Permanent cackler = addReadyCackler(player1);
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
        Permanent cackler = addReadyCackler(player1);
        cackler.setCounterCount(CounterType.OIL, 1);
        addOilPermanent(player1);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(cackler.getPowerModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("The attack boost wears off at end of turn")
    void boostExpiresAtEndOfTurn() {
        Permanent cackler = addReadyCackler(player1);
        addOilPermanent(player1);

        declareAttackers(List.of(0));
        resolveAllTriggers();
        assertThat(cackler.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(cackler.getPowerModifier()).isZero();
    }

    private Permanent addReadyCackler(Player player) {
        Permanent cackler = harness.addToBattlefieldAndReturn(player, new KuldothaCackler());
        cackler.setSummoningSick(false);
        return cackler;
    }

    private void addOilPermanent(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        permanent.setCounterCount(CounterType.OIL, 1);
    }
}
