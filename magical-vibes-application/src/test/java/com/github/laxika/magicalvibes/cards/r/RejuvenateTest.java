package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BlanchwoodTreefolk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Rejuvenate.class, BlanchwoodTreefolk.class})
class RejuvenateTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 6 life")
    void gainsSixLife() {
        harness.setLife(player1, 10);
        harness.castFromHand(player1, new Rejuvenate(), "{3}{G}");
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Cycling discards Rejuvenate and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new Rejuvenate()));
        harness.setLibrary(player1, List.of(new BlanchwoodTreefolk()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Rejuvenate");
        harness.assertInHand(player1, "Blanchwood Treefolk");
    }
}
