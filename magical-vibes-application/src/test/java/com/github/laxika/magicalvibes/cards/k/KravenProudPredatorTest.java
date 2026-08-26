package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.d.DarksteelForge;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KravenProudPredator.class, DarksteelForge.class})
class KravenProudPredatorTest extends BaseCardTest {

    @Test
    @DisplayName("Power is the greatest mana value among permanents you control")
    void powerUsesGreatestControlledPermanentManaValue() {
        Permanent kraven = harness.addToBattlefieldAndReturn(player1, new KravenProudPredator());

        assertThat(gqs.getEffectivePower(gd, kraven)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, kraven)).isEqualTo(4);

        harness.addToBattlefield(player1, new DarksteelForge());

        assertThat(gqs.getEffectivePower(gd, kraven)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, kraven)).isEqualTo(4);
    }

    @Test
    @DisplayName("Opponent permanents do not affect power")
    void ignoresOpponentPermanents() {
        harness.addToBattlefield(player2, new DarksteelForge());
        Permanent kraven = harness.addToBattlefieldAndReturn(player1, new KravenProudPredator());

        assertThat(gqs.getEffectivePower(gd, kraven)).isEqualTo(3);
    }
}
