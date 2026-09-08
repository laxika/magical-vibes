package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TrustedAdvisorTest extends BaseCardTest {

    @Test
    @DisplayName("Controller's maximum hand size is increased by two")
    void controllerMaximumHandSizeIsIncreasedByTwo() {
        harness.addToBattlefield(player1, new TrustedAdvisor());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player1, handOfNineCards());

        gs.advanceStep(gd);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
    }

    @Test
    @DisplayName("Upkeep returns a chosen blue creature the controller controls")
    void upkeepReturnsChosenBlueCreature() {
        addCreatureReady(player1, new TrustedAdvisor());
        Permanent blueCreature = addCreatureReady(player1, new AirElemental());
        Permanent nonBlueCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentBlueCreature = addCreatureReady(player2, new AirElemental());

        advanceToUpkeep(player1);
        resolveAllTriggers();

        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIds()).contains(blueCreature.getId())
                .doesNotContain(nonBlueCreature.getId(), opponentBlueCreature.getId());

        harness.handlePermanentChosen(player1, blueCreature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Air Elemental");
        harness.assertInHand(player1, "Air Elemental");
        harness.assertOnBattlefield(player1, "Trusted Advisor");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Upkeep ability does not trigger during an opponent's upkeep")
    void upkeepAbilityDoesNotTriggerDuringOpponentsUpkeep() {
        addCreatureReady(player1, new TrustedAdvisor());

        advanceToUpkeep(player2);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    private List<Card> handOfNineCards() {
        return List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new Forest(), new Forest(), new Forest(),
                new Plains(), new Plains(), new Plains()
        );
    }
}
