package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.m.MistralCharger;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VisionSkeins.class, MistralCharger.class})
class VisionSkeinsTest extends BaseCardTest {

    @Test
    @DisplayName("Each player draws two cards")
    void eachPlayerDrawsTwoCards() {
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new MistralCharger(), new MistralCharger()));
        harness.setLibrary(player2, List.of(new MistralCharger(), new MistralCharger()));

        harness.castFromHand(player1, new VisionSkeins(), "{1}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }
}
