package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(AdaptiveShimmerer.class)
class AdaptiveShimmererTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with three +1/+1 counters, making it a 3/3")
    void entersWithThreeCounters() {
        harness.setHand(player1, List.of(new AdaptiveShimmerer()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent shimmerer = findPermanent(player1, "Adaptive Shimmerer");
        assertThat(shimmerer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, shimmerer)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, shimmerer)).isEqualTo(3);
    }
}
