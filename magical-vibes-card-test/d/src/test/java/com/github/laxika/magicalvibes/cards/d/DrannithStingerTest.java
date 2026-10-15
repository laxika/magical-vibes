package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.Censor;
import com.github.laxika.magicalvibes.cards.c.Compulsion;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({DrannithStinger.class, DrannithHealer.class, Censor.class, Compulsion.class, GrizzlyBears.class})
class DrannithStingerTest extends BaseCardTest {

    @Test
    @DisplayName("Cycling Stinger costs one generic mana and draws without triggering itself")
    void cyclingStingerDoesNotTriggerItself() {
        harness.setHand(player1, List.of(new DrannithStinger()));
        harness.setLibrary(player1, List.of(new DrannithHealer()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Drannith Stinger");
        harness.assertNotInHand(player1, "Drannith Stinger");
        harness.assertNotInHand(player1, "Drannith Healer");
        harness.passBothPriorities();

        harness.assertInHand(player1, "Drannith Healer");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Cycling a second Stinger triggers the battlefield copy before drawing")
    void cyclingSecondStingerTriggersBeforeDraw() {
        harness.addToBattlefield(player1, new DrannithStinger());
        harness.setHand(player1, List.of(new DrannithStinger()));
        harness.setLibrary(player1, List.of(new DrannithHealer()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Drannith Stinger");
        harness.assertLife(player2, 20);
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        harness.assertNotInHand(player1, "Drannith Healer");
        harness.passBothPriorities();

        harness.assertInHand(player1, "Drannith Healer");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("An opponent cycling does not trigger Stinger")
    void opponentCyclingDoesNotTrigger() {
        harness.addToBattlefield(player1, new DrannithStinger());
        harness.setHand(player2, List.of(new DrannithStinger()));
        harness.setLibrary(player2, List.of(new DrannithHealer()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.activateHandAbility(player2, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Drannith Stinger");
        harness.assertInHand(player2, "Drannith Healer");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Cycling another card deals 1 damage to each opponent")
    void cyclingAnotherCardDealsDamageToEachOpponent() {
        harness.addToBattlefield(player1, new DrannithStinger());
        harness.setHand(player1, List.of(new Censor()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
        harness.assertInGraveyard(player1, "Censor");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("A normal discard does not trigger the cycling ability")
    void normalDiscardDoesNotDealDamage() {
        harness.addToBattlefield(player1, new DrannithStinger());
        harness.addToBattlefield(player1, new Compulsion());
        harness.setHand(player1, List.of(new Censor()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Censor");
        harness.assertInHand(player1, "Grizzly Bears");
    }
}
