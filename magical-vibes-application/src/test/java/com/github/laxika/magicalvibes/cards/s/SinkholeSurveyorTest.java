package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SinkholeSurveyorTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking loses 1 life and lets Sinkhole Surveyor endure with a counter")
    void enduresWithCounter() {
        Permanent surveyor = addCreatureReady(player1, new SinkholeSurveyor());
        harness.setLife(player1, 20);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Put 1 +1/+1 counter on this permanent");

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(surveyor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanents(player1, "Spirit")).isEmpty();
    }

    @Test
    @DisplayName("Attacking loses 1 life and lets Sinkhole Surveyor endure with a Spirit")
    void enduresWithSpirit() {
        addCreatureReady(player1, new SinkholeSurveyor());
        harness.setLife(player1, 20);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Create a 1/1 white Spirit creature token");

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(findPermanents(player1, "Spirit")).hasSize(1);
    }
}
