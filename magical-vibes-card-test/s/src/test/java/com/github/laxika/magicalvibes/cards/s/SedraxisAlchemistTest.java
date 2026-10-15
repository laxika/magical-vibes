package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.Kaleidostone;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SedraxisAlchemist.class, FugitiveWizard.class, GrizzlyBears.class, Forest.class, Kaleidostone.class})
class SedraxisAlchemistTest extends BaseCardTest {


    @Test
    @DisplayName("ETB target is chosen as the trigger goes on the stack, not at cast time")
    void etbTargetChosenAtTriggerTime() {
        setupBluePermanent();
        harness.addToBattlefield(player2, new GrizzlyBears());
        castSedraxisAlchemist();

        // Casting the creature never asks for a target (CR 601.2c) — the gate is intervening-if.
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isNull();

        harness.passBothPriorities(); // resolve creature spell

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("ETB resolves: target nonland permanent is returned to its owner's hand")
    void etbBouncesTargetToOwnersHand() {
        setupBluePermanent();
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        castSedraxisAlchemist();
        harness.passBothPriorities(); // resolve creature spell — trigger-time target prompt

        assertThat(gd.stack).isEmpty();
        harness.handlePermanentChosen(player1, targetId);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        harness.passBothPriorities(); // resolve ETB trigger

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Sedraxis Alchemist enters the battlefield after resolution")
    void alchemistEntersBattlefield() {
        setupBluePermanent();
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        castSedraxisAlchemist();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Sedraxis Alchemist");
    }

    @Test
    @DisplayName("Only nonland permanents are offered as targets — a land is excluded")
    void landIsNotAValidTarget() {
        setupBluePermanent();
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Forest());
        UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");
        UUID landId = harness.getPermanentId(player2, "Forest");
        castSedraxisAlchemist();
        harness.passBothPriorities(); // resolve creature spell — trigger-time target prompt

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(creatureId);
        assertThat(choice.validIds()).doesNotContain(landId);
    }


    @Test
    @DisplayName("ETB does NOT trigger without a blue permanent — no target prompt, no bounce")
    void etbDoesNotTriggerWithoutBluePermanent() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castSedraxisAlchemist();
        harness.passBothPriorities(); // resolve creature spell

        // Intervening-if failed (CR 603.4): no trigger, no target prompt.
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Sedraxis Alchemist");
    }


    @Test
    @DisplayName("ETB does nothing if the blue permanent is gone before resolution")
    void etbFizzlesWhenGateLost() {
        setupBluePermanent();
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        castSedraxisAlchemist();
        harness.passBothPriorities(); // resolve creature spell — trigger-time target prompt
        harness.handlePermanentChosen(player1, targetId); // ETB trigger on stack

        // Remove the blue permanent before the ETB resolves.
        gd.playerBattlefields.get(player1.getId()).removeIf(
                p -> p.getCard().getName().equals("Fugitive Wizard"));

        harness.passBothPriorities(); // resolve ETB trigger — gate no longer met

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("An opponent's blue permanent does not enable the trigger")
    void opponentsBluePermanentDoesNotEnableTrigger() {
        harness.addToBattlefield(player2, new FugitiveWizard());
        castSedraxisAlchemist();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Fugitive Wizard");
    }

    @Test
    @DisplayName("The Alchemist can return itself to its owner's hand")
    void canReturnItself() {
        setupBluePermanent();
        castSedraxisAlchemist();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Sedraxis Alchemist"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Sedraxis Alchemist");
        harness.assertInHand(player1, "Sedraxis Alchemist");
        harness.assertOnBattlefield(player1, "Fugitive Wizard");
    }

    @Test
    @DisplayName("The sole blue permanent can be returned by the trigger")
    void canReturnSoleBluePermanent() {
        setupBluePermanent();
        UUID targetId = harness.getPermanentId(player1, "Fugitive Wizard");
        castSedraxisAlchemist();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Fugitive Wizard");
        harness.assertInHand(player1, "Fugitive Wizard");
        harness.assertOnBattlefield(player1, "Sedraxis Alchemist");
    }

    @Test
    @DisplayName("A noncreature artifact is a legal bounce target")
    void canReturnNoncreatureArtifact() {
        setupBluePermanent();
        harness.addToBattlefield(player2, new Kaleidostone());
        UUID targetId = harness.getPermanentId(player2, "Kaleidostone");
        castSedraxisAlchemist();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Kaleidostone");
        harness.assertInHand(player2, "Kaleidostone");
    }

    @Test
    @DisplayName("Blue mana symbols in an artifact's ability do not make it blue")
    void blueColorIdentityDoesNotEnableTrigger() {
        harness.addToBattlefield(player1, new Kaleidostone());
        castSedraxisAlchemist();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Kaleidostone");
    }

    private void setupBluePermanent() {
        harness.addToBattlefield(player1, new FugitiveWizard());
    }

    private void castSedraxisAlchemist() {
        harness.castFromHand(player1, new SedraxisAlchemist(), "{2}{B}");
    }
}
