package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TwinbladeAssassins.class, Forest.class})
class TwinbladeAssassinsTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card at its controller's end step when a creature died this turn")
    void drawsAtControllerEndStepWhenCreatureDied() {
        harness.addToBattlefield(player1, new TwinbladeAssassins());
        harness.setLibrary(player1, List.of(new Forest()));
        gd.creatureDeathCountThisTurn.merge(player2.getId(), 1, Integer::sum);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);

        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Does not trigger when no creature died this turn")
    void doesNotTriggerWithoutCreatureDeath() {
        harness.addToBattlefield(player1, new TwinbladeAssassins());
        harness.setLibrary(player1, List.of(new Forest()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("Does not trigger on an opponent's end step")
    void doesNotTriggerOnOpponentsEndStep() {
        harness.addToBattlefield(player1, new TwinbladeAssassins());
        harness.setLibrary(player1, List.of(new Forest()));
        gd.creatureDeathCountThisTurn.merge(player2.getId(), 1, Integer::sum);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("Deaths before the Assassins entered count, and multiple deaths draw only one card")
    void earlierDeathsDrawOnlyOneCard() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new TwinbladeAssassins());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new TwinbladeAssassins());
        ownCreature.setMarkedDamage(4);
        opposingCreature.setMarkedDamage(4);
        harness.runStateBasedActions();
        harness.assertInGraveyard(player1, "Twinblade Assassins");
        harness.assertInGraveyard(player2, "Twinblade Assassins");
        harness.addToBattlefield(player1, new TwinbladeAssassins());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A creature dying after the end step begins does not cause a late trigger")
    void deathAfterEndStepBeginsDoesNotTrigger() {
        harness.addToBattlefield(player1, new TwinbladeAssassins());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new TwinbladeAssassins());
        harness.setLibrary(player1, List.of(new Forest()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);
        assertThat(gd.stack).isEmpty();

        opposingCreature.setMarkedDamage(4);
        harness.runStateBasedActions();

        harness.assertInGraveyard(player2, "Twinblade Assassins");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("The draw trigger resolves even if the Assassins die in response")
    void drawResolvesAfterSourceDies() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new TwinbladeAssassins());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new TwinbladeAssassins());
        opposingCreature.setMarkedDamage(4);
        harness.runStateBasedActions();
        harness.setLibrary(player1, List.of(new Forest()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);
        assertThat(gd.stack).hasSize(1);

        source.setMarkedDamage(4);
        harness.runStateBasedActions();
        harness.assertInGraveyard(player1, "Twinblade Assassins");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        harness.assertInHand(player1, "Forest");
    }
}
