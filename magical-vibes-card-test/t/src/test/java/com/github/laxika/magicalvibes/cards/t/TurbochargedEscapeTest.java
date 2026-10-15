package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DuskLegionDreadnought;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TurbochargedEscape.class, DuskLegionDreadnought.class, GrizzlyBears.class, Unsummon.class})
class TurbochargedEscapeTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys non-Vehicle creatures, then permanently animates a chosen Vehicle you control")
    void destroysNonVehicleCreaturesAndAnimatesChosenVehicle() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent firstVehicle = harness.addToBattlefieldAndReturn(player1, new DuskLegionDreadnought());
        Permanent secondVehicle = harness.addToBattlefieldAndReturn(player1, new DuskLegionDreadnought());
        Permanent opponentVehicle = harness.addToBattlefieldAndReturn(player2, new DuskLegionDreadnought());

        cast();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(firstVehicle.getId(), secondVehicle.getId());

        harness.handlePermanentChosen(player1, secondVehicle.getId());

        assertThat(gqs.isCreature(gd, secondVehicle)).isTrue();
        assertThat(gqs.isArtifact(gd, secondVehicle)).isTrue();
        assertThat(gqs.isCreature(gd, firstVehicle)).isFalse();
        assertThat(gqs.isCreature(gd, opponentVehicle)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.CLEANUP);
        assertThat(gqs.isCreature(gd, secondVehicle)).isTrue();
    }

    @Test
    @DisplayName("Does nothing after the wipe when no Vehicle is controlled")
    void noVehicleToAnimate() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        cast();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    private void cast() {
        harness.setHand(player1, List.of(new TurbochargedEscape()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveSorcery(player1, 0, 0);
    }

    @Test
    @DisplayName("An opponent's sole Vehicle cannot be chosen")
    void opponentVehicleIsNotAnimated() {
        Permanent vehicle = harness.addToBattlefieldAndReturn(player2, new DuskLegionDreadnought());
        harness.addToBattlefield(player2, new GrizzlyBears());

        cast();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gqs.isCreature(gd, vehicle)).isFalse();
        harness.assertOnBattlefield(player2, "Dusk Legion Dreadnought");
    }

    @Test
    @DisplayName("Perpetual animation survives returning to hand and being cast again")
    void animationSurvivesReturningToHand() {
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new DuskLegionDreadnought());
        cast();
        assertThat(gqs.isCreature(gd, vehicle)).isTrue();

        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, vehicle.getId());
        harness.assertInHand(player1, "Dusk Legion Dreadnought");
        harness.assertNotOnBattlefield(player1, "Dusk Legion Dreadnought");

        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.isCreature(gd, returned)).isTrue();
        assertThat(gqs.isArtifact(gd, returned)).isTrue();
    }

    @Test
    @DisplayName("Crewed Vehicles survive the wipe and a sole Vehicle is chosen automatically")
    void crewedVehicleSurvivesWipe() {
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new DuskLegionDreadnought());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, vehicle)).isTrue();

        cast();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Dusk Legion Dreadnought");
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.CLEANUP);
        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
    }
}
