package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AirborneAid.class, AvenSoulgazer.class, ElvishWarrior.class, Forest.class})
class AirborneAidTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card for each Bird on either battlefield")
    void drawsForEachBirdOnBattlefield() {
        harness.addToBattlefield(player1, new AvenSoulgazer());
        harness.addToBattlefield(player2, new AvenSoulgazer());
        harness.addToBattlefield(player2, new AvenSoulgazer());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));

        castAirborneAid();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Counts only Birds on the battlefield")
    void ignoresNonBirdPermanents() {
        harness.addToBattlefield(player1, new AvenSoulgazer());
        harness.addToBattlefield(player1, new ElvishWarrior());
        harness.addToBattlefield(player2, new ElvishWarrior());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        castAirborneAid();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not draw when there are no Birds on the battlefield")
    void doesNotDrawWithoutBirds() {
        harness.addToBattlefield(player1, new ElvishWarrior());
        harness.addToBattlefield(player2, new ElvishWarrior());
        harness.setLibrary(player1, List.of(new Forest()));

        castAirborneAid();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    private void castAirborneAid() {
        harness.castFromHand(player1, new AirborneAid(), "{3}{U}");
        harness.passBothPriorities();
    }
}
