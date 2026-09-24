package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DuskLegionDreadnought;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardType;
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

@CardUsed({TurbochargedEscape.class, DuskLegionDreadnought.class, GrizzlyBears.class})
class TurbochargedEscapeTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys non-Vehicle creatures, then permanently animates a chosen Vehicle you control")
    void destroysNonVehicleCreaturesAndAnimatesChosenVehicle() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
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
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
    }
}
