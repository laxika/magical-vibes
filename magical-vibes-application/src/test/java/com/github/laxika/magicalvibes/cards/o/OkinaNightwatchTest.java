package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OkinaNightwatchTest extends BaseCardTest {

    @Test
    void getsBoostWhenControllerHasMoreCardsInHand() {
        harness.setHand(player1, List.of(new Forest(), new Forest()));
        harness.setHand(player2, List.of(new Forest()));
        Permanent nightwatch = harness.addToBattlefieldAndReturn(player1, new OkinaNightwatch());

        assertThat(gqs.getEffectivePower(gd, nightwatch)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, nightwatch)).isEqualTo(6);
    }

    @Test
    void doesNotGetBoostWhenHandSizesAreTiedOrOpponentHasMore() {
        harness.setHand(player1, List.of(new Forest()));
        harness.setHand(player2, List.of(new Forest()));
        Permanent nightwatch = harness.addToBattlefieldAndReturn(player1, new OkinaNightwatch());

        assertThat(gqs.getEffectivePower(gd, nightwatch)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, nightwatch)).isEqualTo(3);

        harness.setHand(player2, List.of(new Forest(), new Forest()));

        assertThat(gqs.getEffectivePower(gd, nightwatch)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, nightwatch)).isEqualTo(3);
    }
}
