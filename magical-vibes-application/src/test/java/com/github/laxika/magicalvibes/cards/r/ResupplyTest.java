package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Resupply.class, Forest.class})
class ResupplyTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 6 life and draws a card")
    void gainsLifeAndDrawsCard() {
        harness.setLife(player1, 10);
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new Resupply()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(16);
        harness.assertInHand(player1, "Forest");
        assertThat(gd.stack).isEmpty();
    }
}
