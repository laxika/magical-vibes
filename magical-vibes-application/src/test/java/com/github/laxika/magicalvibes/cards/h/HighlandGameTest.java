package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.d.Disperse;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

@CardUsed({HighlandGame.class, Shock.class, Disperse.class})
class HighlandGameTest extends BaseCardTest {

    @Test
    @DisplayName("When Highland Game dies, its controller gains 2 life")
    void gainsLifeWhenItDies() {
        harness.addToBattlefield(player2, new HighlandGame());
        harness.setLife(player2, 10);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID highlandGameId = harness.getPermanentId(player2, "Highland Game");
        harness.castAndResolveInstant(player1, 0, highlandGameId);

        harness.assertInGraveyard(player2, "Highland Game");
        harness.passBothPriorities();

        harness.assertLife(player2, 12);
    }

    @Test
    @DisplayName("Life is gained only when the death trigger resolves, and only by its controller")
    void deathTriggerUsesStackAndGainsLifeForController() {
        harness.addToBattlefield(player1, new HighlandGame());
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID highlandGameId = harness.getPermanentId(player1, "Highland Game");
        harness.castAndResolveInstant(player2, 0, highlandGameId);

        harness.assertInGraveyard(player1, "Highland Game");
        harness.assertLife(player1, 10);
        harness.assertLife(player2, 10);

        harness.passBothPriorities();

        harness.assertLife(player1, 12);
        harness.assertLife(player2, 10);
    }

    @Test
    @DisplayName("Returning Highland Game to hand does not trigger life gain")
    void returningToHandDoesNotGainLife() {
        harness.addToBattlefield(player2, new HighlandGame());
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);
        harness.setHand(player1, List.of(new Disperse()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID highlandGameId = harness.getPermanentId(player2, "Highland Game");
        harness.castAndResolveInstant(player1, 0, highlandGameId);

        harness.assertInHand(player2, "Highland Game");
        harness.passBothPriorities();
        harness.assertLife(player1, 10);
        harness.assertLife(player2, 10);
    }
}
