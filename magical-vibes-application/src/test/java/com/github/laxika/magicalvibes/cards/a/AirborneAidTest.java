package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AirborneAid.class, AvenInitiate.class, Forest.class, GrizzlyBears.class})
class AirborneAidTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card for each Bird on either battlefield")
    void drawsForEachBirdOnBattlefield() {
        harness.addToBattlefield(player1, new AvenInitiate());
        harness.addToBattlefield(player2, new AvenInitiate());
        harness.addToBattlefield(player2, new AvenInitiate());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));

        castAirborneAid();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Counts only Birds on the battlefield")
    void ignoresNonBirdPermanents() {
        harness.addToBattlefield(player1, new AvenInitiate());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        castAirborneAid();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    private void castAirborneAid() {
        harness.setHand(player1, List.of(new AirborneAid()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
