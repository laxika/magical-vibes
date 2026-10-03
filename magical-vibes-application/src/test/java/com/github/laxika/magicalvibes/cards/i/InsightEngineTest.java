package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(InsightEngine.class)
class InsightEngineTest extends BaseCardTest {

    @Test
    @DisplayName("Activation adds a charge counter, then draws for all charge counters")
    void activationAddsCounterAndDrawsForAllCounters() {
        Permanent engine = harness.addToBattlefieldAndReturn(player1, new InsightEngine());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(
                new InsightEngine(), new InsightEngine(), new InsightEngine(), new InsightEngine()
        ));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        resolveAllTriggers();

        assertThat(engine.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);

        engine.untap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 0, null, null);
        resolveAllTriggers();

        assertThat(engine.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
    }
}
