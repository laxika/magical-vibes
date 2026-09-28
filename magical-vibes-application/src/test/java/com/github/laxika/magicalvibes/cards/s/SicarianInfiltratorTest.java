package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(SicarianInfiltrator.class)
class SicarianInfiltratorTest extends BaseCardTest {

    @Test
    @DisplayName("Squad creates token copies and each entering Infiltrator draws a card")
    void squadCreatesTokenCopiesAndDrawsForEachEntry() {
        harness.setHand(player1, List.of(new SicarianInfiltrator()));
        harness.setLibrary(player1, List.of(
                new SicarianInfiltrator(), new SicarianInfiltrator(), new SicarianInfiltrator()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castCreatureWithRepeatedCosts(player1, 0, List.of("{2}", "{2}"));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Sicarian Infiltrator")).hasSize(3);
        assertThat(findPermanents(player1, "Sicarian Infiltrator"))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }
}
