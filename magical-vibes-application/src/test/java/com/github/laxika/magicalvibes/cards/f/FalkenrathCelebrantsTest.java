package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(FalkenrathCelebrants.class)
class FalkenrathCelebrantsTest extends BaseCardTest {

    @Test
    @DisplayName("When Falkenrath Celebrants enters, two Blood tokens are created")
    void etbCreatesTwoBloodTokens() {
        harness.setHand(player1, List.of(new FalkenrathCelebrants()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> bloodTokens = findPermanents(player1, "Blood");
        assertThat(bloodTokens).hasSize(2);
        assertThat(bloodTokens).allMatch(permanent -> permanent.getCard().isToken());
    }
}
