package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.Gravedigger;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ErebossTitan.class, Gravedigger.class, GrizzlyBears.class, Murder.class})
class ErebossTitanTest extends BaseCardTest {

    /**
     * Puts Erebos's Titan into player1's graveyard and has player2 cast Gravedigger, returning a
     * creature card from player2's graveyard to their hand — the event Erebos's Titan watches for.
     * Leaves the Titan's may prompt pending.
     */
    private void opponentReturnsCreatureFromTheirGraveyard() {
        harness.setGraveyard(player1, List.of(new ErebossTitan()));
        triggerOpponentGraveyardDeparture();
    }

    private void triggerOpponentGraveyardDeparture() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setGraveyard(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.setHand(player2, new ArrayList<>(List.of(new Gravedigger())));
        harness.addMana(player2, ManaColor.BLACK, 4);

        harness.castCreature(player2, 0);
        harness.passBothPriorities(); // Gravedigger enters
        harness.handleMultipleCardsChosen(player2,
                List.of(gd.playerGraveyards.get(player2.getId()).getFirst().getId()));
        harness.passBothPriorities(); // ETB may prompt
        harness.handleMayAbilityChosen(player2, true);

        harness.passBothPriorities(); // resolve Erebos's Titan's trigger → may prompt
    }

    @Test
    @DisplayName("Has indestructible while opponents control no creatures")
    void indestructibleWithNoOpponentCreatures() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent titan = harness.addToBattlefieldAndReturn(player1, new ErebossTitan());

        harness.setHand(player2, new ArrayList<>(List.of(new Murder())));
        harness.addMana(player2, ManaColor.BLACK, 4);
        harness.castAndResolveInstant(player2, 0, titan.getId());

        harness.assertOnBattlefield(player1, "Erebos's Titan");
        harness.assertNotInGraveyard(player1, "Erebos's Titan");
    }

    @Test
    @DisplayName("Loses indestructible while an opponent controls a creature")
    void destructibleWhenOpponentControlsCreature() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent titan = harness.addToBattlefieldAndReturn(player1, new ErebossTitan());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player2, new ArrayList<>(List.of(new Murder())));
        harness.addMana(player2, ManaColor.BLACK, 4);
        harness.castAndResolveInstant(player2, 0, titan.getId());

        harness.assertNotOnBattlefield(player1, "Erebos's Titan");
        harness.assertInGraveyard(player1, "Erebos's Titan");
    }

    @Test
    @DisplayName("Creature card leaving an opponent's graveyard triggers; discarding returns the Titan to hand")
    void discardReturnsTitanToHand() {
        opponentReturnsCreatureFromTheirGraveyard();
        harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears())));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)
                .playerId()).isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Grizzly Bears");

        harness.assertInHand(player1, "Erebos's Titan");
        harness.assertNotInGraveyard(player1, "Erebos's Titan");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Declining the trigger leaves the Titan in the graveyard")
    void decliningLeavesTitanInGraveyard() {
        opponentReturnsCreatureFromTheirGraveyard();
        harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears())));

        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Erebos's Titan");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("A creature card leaving your own graveyard does not trigger")
    void ownGraveyardDoesNotTrigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setGraveyard(player1, new ArrayList<>(List.of(new ErebossTitan(), new GrizzlyBears())));
        harness.setHand(player1, new ArrayList<>(List.of(new Gravedigger())));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1,
                List.of(gd.playerGraveyards.get(player1.getId()).get(1).getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Erebos's Titan");
    }

    @Test
    @DisplayName("An empty hand cannot pay for returning the Titan")
    void emptyHandDoesNotReturnTitan() {
        opponentReturnsCreatureFromTheirGraveyard();
        harness.setHand(player1, List.of());

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Erebos's Titan");
        harness.assertNotInHand(player1, "Erebos's Titan");
    }

    @Test
    @DisplayName("Lethal damage remains marked and destroys the Titan when it loses indestructible")
    void lethalDamageDestroysTitanWhenOpponentGainsCreature() {
        Permanent titan = harness.addToBattlefieldAndReturn(player1, new ErebossTitan());
        titan.setMarkedDamage(5);
        harness.runStateBasedActions();
        harness.assertOnBattlefield(player1, "Erebos's Titan");

        harness.addToBattlefield(player2, new ErebossTitan());
        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Erebos's Titan");
        harness.assertInGraveyard(player1, "Erebos's Titan");
    }

    @Test
    @DisplayName("The graveyard ability does not trigger while the Titan is on the battlefield")
    void battlefieldTitanDoesNotTrigger() {
        harness.addToBattlefield(player1, new ErebossTitan());
        harness.setGraveyard(player1, List.of());
        triggerOpponentGraveyardDeparture();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player1, "Erebos's Titan");
    }

    @Test
    @DisplayName("A Titan that died normally can return after discarding a card")
    void returnsTitanThatEnteredGraveyardThroughGameActions() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent titan = harness.addToBattlefieldAndReturn(player1, new ErebossTitan());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Murder(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player1, 0, titan.getId());
        harness.assertInGraveyard(player1, "Erebos's Titan");

        triggerOpponentGraveyardDeparture();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Erebos's Titan");
        harness.assertNotInGraveyard(player1, "Erebos's Titan");
    }
}
