package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({KazanduNectarpot.class, Forest.class})
class KazanduNectarpotTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 1 life when a land you control enters")
    void gainsLifeWhenControllerPlaysLand() {
        harness.addToBattlefield(player1, new KazanduNectarpot());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Does not trigger when an opponent's land enters")
    void doesNotTriggerForOpponentsLand() {
        harness.addToBattlefield(player1, new KazanduNectarpot());
        harness.setLife(player1, 20);
        harness.setHand(player2, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.playLand(player2, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }
}
