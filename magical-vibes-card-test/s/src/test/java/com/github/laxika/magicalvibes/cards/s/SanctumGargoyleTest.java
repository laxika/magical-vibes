package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.CouriersCapsule;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SanctumGargoyle.class, Ornithopter.class, GrizzlyBears.class, CouriersCapsule.class})
class SanctumGargoyleTest extends BaseCardTest {

    /** Casts Sanctum Gargoyle and resolves the creature so its ETB sets up graveyard targeting. */
    private void castSanctumGargoyle() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new SanctumGargoyle(), "{3}{W}");
        harness.passBothPriorities(); // resolve creature → ETB triggers graveyard targeting
    }

    @Test
    @DisplayName("ETB returns a targeted artifact card from graveyard to hand")
    void etbReturnsArtifactToHand() {
        Ornithopter ornithopter = new Ornithopter();
        harness.setGraveyard(player1, List.of(ornithopter));

        castSanctumGargoyle();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(ornithopter.getId()));
        harness.passBothPriorities(); // resolve the ETB triggered ability
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Ornithopter");
        harness.assertNotInGraveyard(player1, "Ornithopter");
    }

    @Test
    @DisplayName("A non-artifact card in the graveyard is not a legal target")
    void nonArtifactNotTargetable() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        castSanctumGargoyle();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The optional return can be declined")
    void returnCanBeDeclined() {
        Ornithopter ornithopter = new Ornithopter();
        harness.setGraveyard(player1, List.of(ornithopter));

        castSanctumGargoyle();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);

        // The target is required; the return is optional at resolution.
        harness.handleMultipleCardsChosen(player1, List.of(ornithopter.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Ornithopter");
        harness.assertNotInHand(player1, "Ornithopter");
    }

    @Test
    @DisplayName("Empty graveyard leaves no targeted ability on the stack")
    void emptyGraveyardLeavesNoTargetedAbility() {
        castSanctumGargoyle();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("ETB can return a noncreature artifact and returns only the chosen card")
    void returnsOnlyChosenNoncreatureArtifact() {
        CouriersCapsule capsule = new CouriersCapsule();
        SanctumGargoyle otherGargoyle = new SanctumGargoyle();
        harness.setGraveyard(player1, List.of(capsule, otherGargoyle));

        castSanctumGargoyle();

        harness.handleMultipleCardsChosen(player1, List.of(capsule.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Courier's Capsule");
        harness.assertNotInGraveyard(player1, "Courier's Capsule");
        harness.assertInGraveyard(player1, "Sanctum Gargoyle");
        harness.assertNotInHand(player1, "Sanctum Gargoyle");
    }

    @Test
    @DisplayName("An artifact in an opponent's graveyard is not a legal target")
    void cannotTargetOpponentsGraveyard() {
        harness.setGraveyard(player2, List.of(new SanctumGargoyle()));

        castSanctumGargoyle();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player2, "Sanctum Gargoyle");
        harness.assertNotInHand(player1, "Sanctum Gargoyle");
    }

    @Test
    @DisplayName("A target removed from the graveyard before resolution is not returned")
    void removedTargetIsNotReturned() {
        CouriersCapsule capsule = new CouriersCapsule();
        harness.setGraveyard(player1, List.of(capsule));

        castSanctumGargoyle();
        harness.handleMultipleCardsChosen(player1, List.of(capsule.getId()));
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotInHand(player1, "Courier's Capsule");
    }
}
