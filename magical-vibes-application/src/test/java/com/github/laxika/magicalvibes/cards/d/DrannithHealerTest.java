package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.Compulsion;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({DrannithHealer.class, Compulsion.class})
class DrannithHealerTest extends BaseCardTest {

    @Test
    @DisplayName("Cycling another card makes you gain 1 life")
    void cyclingAnotherCardGainsLife() {
        harness.addToBattlefield(player1, new DrannithHealer());
        harness.setHand(player1, List.of(new DrannithHealer()));
        harness.setLibrary(player1, List.of(new DrannithHealer()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertInGraveyard(player1, "Drannith Healer");
        harness.assertInHand(player1, "Drannith Healer");
    }

    @Test
    @DisplayName("A normal discard does not trigger the cycling ability")
    void normalDiscardDoesNotGainLife() {
        harness.addToBattlefield(player1, new DrannithHealer());
        harness.addToBattlefield(player1, new Compulsion());
        harness.setHand(player1, List.of(new DrannithHealer()));
        harness.setLibrary(player1, List.of(new DrannithHealer()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Drannith Healer");
        harness.assertInHand(player1, "Drannith Healer");
    }

    @Test
    @DisplayName("Cycling Healer from hand draws a card without gaining life")
    void cyclingHealerItselfDoesNotGainLife() {
        harness.setHand(player1, List.of(new DrannithHealer()));
        harness.setLibrary(player1, List.of(new DrannithHealer()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.assertInGraveyard(player1, "Drannith Healer");
        harness.assertNotInHand(player1, "Drannith Healer");
        harness.assertLife(player1, 20);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Drannith Healer");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("An opponent cycling does not trigger your Healer")
    void opponentCyclingDoesNotGainLife() {
        harness.addToBattlefield(player1, new DrannithHealer());
        harness.setHand(player2, List.of(new DrannithHealer()));
        harness.setLibrary(player2, List.of(new DrannithHealer()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.activateHandAbility(player2, 0, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player2, "Drannith Healer");
        harness.assertInHand(player2, "Drannith Healer");
    }

    @Test
    @DisplayName("Each Healer gains life on the stack before the cycling draw")
    void multipleHealersTriggerBeforeCyclingDraw() {
        harness.addToBattlefield(player1, new DrannithHealer());
        harness.addToBattlefield(player1, new DrannithHealer());
        harness.setHand(player1, List.of(new DrannithHealer()));
        harness.setLibrary(player1, List.of(new DrannithHealer()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.assertLife(player1, 20);
        harness.assertNotInHand(player1, "Drannith Healer");

        harness.passBothPriorities();
        harness.assertLife(player1, 21);
        harness.assertNotInHand(player1, "Drannith Healer");

        harness.passBothPriorities();
        harness.assertLife(player1, 22);
        harness.assertNotInHand(player1, "Drannith Healer");

        harness.passBothPriorities();
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
        harness.assertInHand(player1, "Drannith Healer");
    }
}
