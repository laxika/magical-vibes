package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ImposingVantasaur.class, GrizzlyBears.class})
class ImposingVantasaurTest extends BaseCardTest {

    @Test
    @DisplayName("Cycling {1} discards Imposing Vantasaur and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new ImposingVantasaur()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Imposing Vantasaur");
        harness.assertInHand(player1, "Grizzly Bears");
    }
}
