package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({LightkeeperOfEmeria.class})
class LightkeeperOfEmeriaTest extends BaseCardTest {

    @Test
    @DisplayName("Without multikicker, it gains no life")
    void gainsNoLifeWithoutMultikicker() {
        harness.setHand(player1, List.of(new LightkeeperOfEmeria()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("It gains 2 life for one multikicker payment")
    void gainsTwoLifeForOneMultikickerPayment() {
        harness.setHand(player1, List.of(new LightkeeperOfEmeria()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithRepeatedCosts(player1, 0, List.of("{W}"));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("It gains 2 life for each multikicker payment")
    void gainsTwoLifePerMultikickerPayment() {
        harness.setHand(player1, List.of(new LightkeeperOfEmeria()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithRepeatedCosts(player1, 0, List.of("{W}", "{W}"));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 24);
    }

    @Test
    @DisplayName("The other player gains life only when the enters trigger resolves")
    void otherPlayerGainsLifeWhenTriggerResolves() {
        gd.activePlayerId = player2.getId();
        harness.setHand(player2, List.of(new LightkeeperOfEmeria()));
        harness.addMana(player2, ManaColor.WHITE, 4);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castCreatureWithRepeatedCosts(player2, 0, List.of("{W}", "{W}", "{W}"));
        harness.assertLife(player2, 20);

        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Lightkeeper of Emeria");
        harness.assertLife(player2, 20);
        harness.assertLife(player1, 20);

        harness.passBothPriorities();
        harness.assertLife(player2, 26);
        harness.assertLife(player1, 20);
    }
}
