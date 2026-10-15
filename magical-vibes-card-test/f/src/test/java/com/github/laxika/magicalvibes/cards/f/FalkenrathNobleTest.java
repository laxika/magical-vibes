package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.b.BlasphemousAct;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FalkenrathNoble.class, GrizzlyBears.class, Shock.class})
class FalkenrathNobleTest extends BaseCardTest {


    @Test
    @DisplayName("When Falkenrath Noble dies, target player loses 1 life and controller gains 1 life")
    void selfDeathDrainsTargetPlayer() {
        harness.addToBattlefield(player1, new FalkenrathNoble());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        // Kill Falkenrath Noble with Shock
        setupPlayer2Active();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID nobleId = harness.getPermanentId(player1, "Falkenrath Noble");
        harness.castAndResolveInstant(player2, 0, nobleId);

        // Player1 is prompted to choose a target player
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        // Choose opponent as target
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities(); // Resolve death trigger

        // Target player loses 1 life, controller gains 1 life
        harness.assertLife(player2, 19);
        harness.assertLife(player1, 21);
    }


    @Test
    @DisplayName("When an ally creature dies, target player loses 1 life and controller gains 1 life")
    void allyCreatureDeathDrainsTargetPlayer() {
        harness.addToBattlefield(player1, new FalkenrathNoble());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        // Kill ally creature with Shock
        setupPlayer2Active();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castAndResolveInstant(player2, 0, bearsId);

        // Player1 is prompted to choose a target player
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        // Choose opponent as target
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities(); // Resolve death trigger

        // Target player loses 1 life, controller gains 1 life
        harness.assertLife(player2, 19);
        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("When an opponent's creature dies, target player loses 1 life and controller gains 1 life")
    void opponentCreatureDeathDrainsTargetPlayer() {
        harness.addToBattlefield(player1, new FalkenrathNoble());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        // Kill opponent's creature with Shock
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, bearsId);

        // Player1 is prompted to choose a target player
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        // Choose opponent as target
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities(); // Resolve death trigger

        // Target player loses 1 life, controller gains 1 life
        harness.assertLife(player2, 19);
        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Death trigger can target the controller for life loss")
    void deathTriggerCanTargetSelf() {
        harness.addToBattlefield(player1, new FalkenrathNoble());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        // Kill opponent's creature with Shock
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, bearsId);

        // Choose self as target
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities(); // Resolve death trigger

        // Controller loses 1 life AND gains 1 life (net 0)
        harness.assertLife(player1, 20);
        // Opponent unaffected
        harness.assertLife(player2, 20);
    }

    @Test
    @CardUsed({BlasphemousAct.class})
    @DisplayName("Each Noble sees both deaths when two Nobles die simultaneously")
    void simultaneousDeathsTriggerEachNobleForEachCreature() {
        harness.addToBattlefield(player1, new FalkenrathNoble());
        harness.addToBattlefield(player1, new FalkenrathNoble());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castFromHand(player1, new BlasphemousAct(), "{8}{R}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Falkenrath Noble");
        for (int i = 0; i < 4; i++) {
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
            harness.handlePermanentChosen(player1, player2.getId());
        }
        assertThat(gd.stack).hasSize(4);
        for (int i = 0; i < 4; i++) {
            harness.passBothPriorities();
        }

        harness.assertLife(player1, 24);
        harness.assertLife(player2, 16);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent-controlled Noble gains life for its own controller")
    void opponentControlledNobleDrainsForOpponent() {
        harness.addToBattlefield(player2, new FalkenrathNoble());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Falkenrath Noble"));
        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 21);
    }

    private void setupPlayer2Active() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
