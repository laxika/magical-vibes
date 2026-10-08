package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.b.BartizanBats;
import com.github.laxika.magicalvibes.cards.d.DazzlingLights;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WhisperingSnitch.class, DazzlingLights.class, BartizanBats.class})
class WhisperingSnitchTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage and gains life on the first surveil each turn")
    void triggersOnlyOnFirstSurveilEachTurn() {
        addCreatureReady(player1, new WhisperingSnitch());
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player2, new BartizanBats());
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player2, new BartizanBats());
        List<Card> library = List.of(new BartizanBats(), new BartizanBats(), new BartizanBats(), new BartizanBats());
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new DazzlingLights(), new DazzlingLights()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        int player1Life = gd.getLife(player1.getId());
        int player2Life = gd.getLife(player2.getId());

        harness.castInstant(player1, 0, firstTarget.getId());
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));
        harness.passBothPriorities();

        harness.castInstant(player1, 0, secondTarget.getId());
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));
        harness.passBothPriorities();

        harness.assertLife(player1, player1Life + 1);
        harness.assertLife(player2, player2Life - 1);
    }

    @Test
    @DisplayName("Does not trigger after entering following the controller's first surveil")
    void doesNotTriggerAfterEnteringFollowingFirstSurveil() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BartizanBats());
        harness.setLibrary(player1, List.of(new BartizanBats(), new BartizanBats()));
        harness.setHand(player1, List.of(new DazzlingLights(), new WhisperingSnitch(), new DazzlingLights()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        int controllerLife = gd.getLife(player1.getId());
        int opponentLife = gd.getLife(player2.getId());

        surveilTwo(player1, target);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Whispering Snitch");
        harness.addMana(player1, ManaColor.BLUE, 1);
        surveilTwo(player1, target);
        harness.passBothPriorities();

        harness.assertLife(player1, controllerLife);
        harness.assertLife(player2, opponentLife);
    }

    @Test
    @DisplayName("Opponent surveil neither triggers nor consumes the controller's first surveil")
    void opponentSurveilDoesNotCountForController() {
        addCreatureReady(player1, new WhisperingSnitch());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BartizanBats());
        harness.setLibrary(player1, List.of(new BartizanBats(), new BartizanBats()));
        harness.setLibrary(player2, List.of(new BartizanBats(), new BartizanBats()));
        harness.setHand(player1, List.of(new DazzlingLights()));
        harness.setHand(player2, List.of(new DazzlingLights()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);
        int controllerLife = gd.getLife(player1.getId());
        int opponentLife = gd.getLife(player2.getId());

        surveilTwo(player2, target);
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, controllerLife);
        harness.assertLife(player2, opponentLife);

        surveilTwo(player1, target);
        harness.passBothPriorities();
        harness.assertLife(player1, controllerLife + 1);
        harness.assertLife(player2, opponentLife - 1);
    }

    @Test
    @DisplayName("Surveilling an empty library still triggers")
    void triggersWithEmptyLibrary() {
        addCreatureReady(player1, new WhisperingSnitch());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BartizanBats());
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new DazzlingLights()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        int controllerLife = gd.getLife(player1.getId());
        int opponentLife = gd.getLife(player2.getId());

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, controllerLife + 1);
        harness.assertLife(player2, opponentLife - 1);
    }

    @Test
    @DisplayName("Every Snitch triggers on the same first surveil")
    void multipleSnitchesEachTrigger() {
        addCreatureReady(player1, new WhisperingSnitch());
        addCreatureReady(player1, new WhisperingSnitch());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BartizanBats());
        harness.setLibrary(player1, List.of(new BartizanBats(), new BartizanBats()));
        harness.setHand(player1, List.of(new DazzlingLights()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        int controllerLife = gd.getLife(player1.getId());
        int opponentLife = gd.getLife(player2.getId());

        surveilTwo(player1, target);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, controllerLife + 2);
        harness.assertLife(player2, opponentLife - 2);
    }

    @Test
    @DisplayName("The controller's first surveil on an opponent's turn triggers again")
    void triggersAgainOnOpponentsTurn() {
        addCreatureReady(player1, new WhisperingSnitch());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BartizanBats());
        harness.setLibrary(player1, List.of(new BartizanBats(), new BartizanBats()));
        harness.setHand(player1, List.of(new DazzlingLights(), new DazzlingLights()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        int controllerLife = gd.getLife(player1.getId());
        int opponentLife = gd.getLife(player2.getId());

        surveilTwo(player1, target);
        harness.passBothPriorities();
        harness.assertLife(player1, controllerLife + 1);
        harness.assertLife(player2, opponentLife - 1);
        harness.setLibrary(player2, List.of(new BartizanBats(), new BartizanBats()));
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 1);
        surveilTwo(player1, target);
        harness.passBothPriorities();

        harness.assertLife(player1, controllerLife + 2);
        harness.assertLife(player2, opponentLife - 2);
    }

    private void surveilTwo(Player player, Permanent target) {
        harness.castInstant(player, 0, target.getId());
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));
    }
}
