package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.Gravedigger;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ErebossTitanTest extends BaseCardTest {

    /**
     * Puts Erebos's Titan into player1's graveyard and has player2 cast Gravedigger, returning a
     * creature card from player2's graveyard to their hand — the event Erebos's Titan watches for.
     * Leaves the Titan's may prompt pending.
     */
    private void opponentReturnsCreatureFromTheirGraveyard() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setGraveyard(player1, new ArrayList<>(List.of(new ErebossTitan())));
        harness.setGraveyard(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.setHand(player2, new ArrayList<>(List.of(new Gravedigger())));
        harness.addMana(player2, ManaColor.BLACK, 4);

        harness.castCreature(player2, 0);
        harness.passBothPriorities(); // Gravedigger enters
        harness.passBothPriorities(); // ETB may prompt
        harness.handleMayAbilityChosen(player2, true);
        harness.handleGraveyardCardChosen(player2, 0); // Grizzly Bears leaves player2's graveyard

        harness.passBothPriorities(); // resolve Erebos's Titan's trigger → may prompt
    }

    @Test
    @DisplayName("Has indestructible while opponents control no creatures")
    void indestructibleWithNoOpponentCreatures() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new ErebossTitan());

        Permanent titan = gd.playerBattlefields.get(player1.getId()).getFirst();

        harness.setHand(player2, new ArrayList<>(List.of(new Murder())));
        harness.addMana(player2, ManaColor.BLACK, 4);
        harness.castInstant(player2, 0, titan.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Erebos's Titan");
        harness.assertNotInGraveyard(player1, "Erebos's Titan");
    }

    @Test
    @DisplayName("Loses indestructible while an opponent controls a creature")
    void destructibleWhenOpponentControlsCreature() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new ErebossTitan());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent titan = gd.playerBattlefields.get(player1.getId()).getFirst();

        harness.setHand(player2, new ArrayList<>(List.of(new Murder())));
        harness.addMana(player2, ManaColor.BLACK, 4);
        harness.castInstant(player2, 0, titan.getId());
        harness.passBothPriorities();

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

        harness.passBothPriorities(); // resolve the reflexive return trigger

        harness.assertInHand(player1, "Erebos's Titan");
        harness.assertNotInGraveyard(player1, "Erebos's Titan");
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
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleGraveyardCardChosen(player1, 1); // Grizzly Bears leaves player1's own graveyard

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Erebos's Titan");
    }
}
