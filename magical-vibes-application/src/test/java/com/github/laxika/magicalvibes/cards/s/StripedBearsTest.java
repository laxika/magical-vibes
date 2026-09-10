package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Striped Bears")
@CardUsed(StripedBears.class)
class StripedBearsTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield draws a card")
    void etbDrawsACard() {
        harness.setLibrary(player1, List.of(new StripedBears()));
        harness.castFromHand(player1, new StripedBears(), "{3}{G}");

        harness.passBothPriorities(); // resolve creature spell → ETB trigger on stack
        harness.passBothPriorities(); // resolve ETB trigger → draw

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Striped Bears");
        harness.assertInHand(player1, "Striped Bears");
    }
}
