package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GildedDrake;
import com.github.laxika.magicalvibes.cards.g.GorillaWarrior;
import com.github.laxika.magicalvibes.cards.i.IvoryMask;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TaintedAether.class, Forest.class, GorillaWarrior.class, GildedDrake.class, IvoryMask.class})
class TaintedAetherTest extends BaseCardTest {

    @Test
    @DisplayName("A creature entering under the controller triggers Tainted Aether")
    void triggersWhenControllerCreatureEnters() {
        Permanent aether = harness.addToBattlefieldAndReturn(player1, new TaintedAether());

        harness.castFromHand(player1, new GorillaWarrior(), "{2}{G}");

        harness.passBothPriorities(); // resolve creature spell → Tainted Aether triggers

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getSourcePermanentId()).isEqualTo(aether.getId());
    }

    @Test
    @DisplayName("Entering creature's controller sacrifices a chosen creature or land")
    void controllerChoosesCreatureOrLandToSacrifice() {
        harness.addToBattlefield(player1, new TaintedAether());
        harness.addToBattlefield(player1, new Forest());

        harness.castFromHand(player1, new GorillaWarrior(), "{2}{G}");

        harness.passBothPriorities(); // resolve creature spell → trigger
        harness.passBothPriorities(); // resolve trigger → controller prompted to choose

        // Two valid permanents (the entering creature + the Forest) → the controller must choose.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).playerId())
                .isEqualTo(player1.getId());

        UUID forestId = harness.getPermanentId(player1, "Forest");
        harness.handleMultiplePermanentsChosen(player1, List.of(forestId));

        // The Forest was sacrificed; the entering creature stays.
        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player1, "Gorilla Warrior");
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("With only the entering creature as a valid permanent, it is auto-sacrificed")
    void autoSacrificesWhenSingleValidPermanent() {
        harness.addToBattlefield(player1, new TaintedAether());

        harness.castFromHand(player1, new GorillaWarrior(), "{2}{G}");

        harness.passBothPriorities(); // resolve creature spell → trigger
        harness.passBothPriorities(); // resolve trigger → only the creature is valid

        // The enchantment isn't a creature or land, so the entering creature sacrifices itself.
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Gorilla Warrior");
        harness.assertInGraveyard(player1, "Gorilla Warrior");
    }

    @Test
    @DisplayName("A creature entering under an opponent makes that opponent sacrifice")
    void triggersForOpponentCreature() {
        harness.addToBattlefield(player1, new TaintedAether());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new GorillaWarrior(), "{2}{G}");

        harness.passBothPriorities(); // resolve creature spell → trigger
        harness.passBothPriorities(); // resolve trigger → opponent sacrifices

        // The entering creature's controller (player2), not Tainted Aether's controller, sacrifices.
        harness.assertInGraveyard(player2, "Gorilla Warrior");
        harness.assertOnBattlefield(player1, "Tainted Aether");
    }

    @Test
    @DisplayName("A land entering does not trigger Tainted Aether")
    void doesNotTriggerForLand() {
        harness.addToBattlefield(player1, new TaintedAether());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("A player's shroud does not stop the non-targeting sacrifice ability")
    void shroudDoesNotStopSacrificeAbility() {
        harness.addToBattlefield(player1, new TaintedAether());
        harness.addToBattlefield(player2, new IvoryMask());
        harness.addToBattlefield(player2, new Forest());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new GorillaWarrior(), "{2}{G}");

        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validIds()).containsExactlyInAnyOrder(
                harness.getPermanentId(player2, "Forest"),
                harness.getPermanentId(player2, "Gorilla Warrior"));
    }

    @Test
    @DisplayName("The creature's controller at resolution chooses what to sacrifice")
    void usesCreatureControllerAtResolution() {
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player1, new TaintedAether());
        Permanent gorilla = harness.addToBattlefieldAndReturn(player2, new GorillaWarrior());

        harness.castFromHand(player1, new GildedDrake(), "{1}{U}");

        harness.passBothPriorities();
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.ETBTokenMultiTargetTrigger.class);
        harness.handlePermanentChosen(player1, gorilla.getId());
        harness.passBothPriorities(); // Gilded Drake exchanges control before Tainted Aether resolves.

        harness.assertOnBattlefield(player2, "Gilded Drake");
        harness.assertOnBattlefield(player1, "Gorilla Warrior");

        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validIds()).containsExactlyInAnyOrder(
                harness.getPermanentId(player2, "Gilded Drake"),
                harness.getPermanentId(player2, "Forest"));
    }
}
