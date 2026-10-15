package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StampedingSerow.class, GrizzlyBears.class, AirElemental.class, Unsummon.class})
class StampedingSerowTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep returns a chosen green creature the controller controls")
    void upkeepReturnsChosenGreenCreature() {
        Permanent serow = addCreatureReady(player1, new StampedingSerow());
        Permanent greenCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent nonGreenCreature = addCreatureReady(player1, new AirElemental());
        Permanent opponentGreenCreature = addCreatureReady(player2, new GrizzlyBears());

        advanceToUpkeep(player1);
        resolveAllTriggers();

        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIds()).contains(serow.getId(), greenCreature.getId())
                .doesNotContain(nonGreenCreature.getId(), opponentGreenCreature.getId());

        harness.handlePermanentChosen(player1, greenCreature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Stampeding Serow");
        harness.assertOnBattlefield(player1, "Air Elemental");
    }

    @Test
    @DisplayName("Upkeep ability does not trigger during an opponent's upkeep")
    void upkeepAbilityDoesNotTriggerDuringOpponentsUpkeep() {
        addCreatureReady(player1, new StampedingSerow());

        advanceToUpkeep(player2);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    @Test
    @DisplayName("Serow must return itself when it is the only green creature")
    void returnsItselfWhenItIsTheOnlyGreenCreature() {
        Permanent serow = addCreatureReady(player1, new StampedingSerow());
        addCreatureReady(player1, new AirElemental());

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(serow.getId());
        harness.handlePermanentChosen(player1, serow.getId());

        harness.assertNotOnBattlefield(player1, "Stampeding Serow");
        harness.assertInHand(player1, "Stampeding Serow");
        harness.assertOnBattlefield(player1, "Air Elemental");
    }

    @Test
    @DisplayName("A controlled creature returns to its owner rather than its controller")
    void returnsCreatureToItsOwnersHand() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        addCreatureReady(player1, new StampedingSerow());
        GrizzlyBears bears = new GrizzlyBears();
        bears.setOwnerId(player2.getId());
        Permanent borrowedCreature = addCreatureReady(player1, bears);

        advanceToUpkeep(player1);
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, borrowedCreature.getId());

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertNotInHand(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Stampeding Serow");
    }

    @Test
    @DisplayName("Upkeep trigger still returns a creature after Serow leaves the battlefield")
    void triggerResolvesAfterSerowLeaves() {
        Permanent serow = addCreatureReady(player1, new StampedingSerow());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Unsummon()));

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, serow.getId());
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(bears.getId());
        harness.handlePermanentChosen(player1, bears.getId());

        harness.assertInHand(player1, "Stampeding Serow");
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Upkeep trigger does nothing if no green creature remains at resolution")
    void triggerDoesNothingWithoutGreenCreaturesAtResolution() {
        Permanent serow = addCreatureReady(player1, new StampedingSerow());
        addCreatureReady(player1, new AirElemental());
        harness.setHand(player1, List.of(new Unsummon()));

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, serow.getId());
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInHand(player1, "Stampeding Serow");
        harness.assertOnBattlefield(player1, "Air Elemental");
    }
}
