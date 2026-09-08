package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
}
