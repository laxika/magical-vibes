package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DoctorOctopusMasterPlanner.class, DocOckSinisterScientist.class, GrizzlyBears.class})
class DoctorOctopusMasterPlannerTest extends BaseCardTest {

    @Test
    @DisplayName("Other Villains you control get +2/+2")
    void boostsOtherVillainsYouControl() {
        Permanent doctor = harness.addToBattlefieldAndReturn(player1, new DoctorOctopusMasterPlanner());
        Permanent ownVillain = harness.addToBattlefieldAndReturn(player1, new DocOckSinisterScientist());
        Permanent ownNonVillain = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingVillain = harness.addToBattlefieldAndReturn(player2, new DocOckSinisterScientist());

        assertThat(gqs.getEffectivePower(gd, doctor)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, doctor)).isEqualTo(8);
        assertThat(gqs.getEffectivePower(gd, ownVillain)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, ownVillain)).isEqualTo(7);
        assertThat(gqs.getEffectivePower(gd, ownNonVillain)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownNonVillain)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposingVillain)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, opposingVillain)).isEqualTo(5);
    }

    @Test
    @DisplayName("Raises its controller's maximum hand size to eight")
    void maximumHandSizeIsEight() {
        harness.addToBattlefield(player1, new DoctorOctopusMasterPlanner());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player1, cards(9));

        gs.advanceStep(gd);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount())
                .isEqualTo(1);
    }

    @Test
    @DisplayName("Draws exactly enough cards to reach eight at the controller's end step")
    void drawsUpToEightCardsAtEndStep() {
        harness.addToBattlefield(player1, new DoctorOctopusMasterPlanner());
        harness.setHand(player1, cards(5));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        gs.advanceStep(gd);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(8);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 3);
    }

    @Test
    @DisplayName("Does not draw when the controller already has eight cards")
    void doesNotDrawAtEightCards() {
        harness.addToBattlefield(player1, new DoctorOctopusMasterPlanner());
        harness.setHand(player1, cards(8));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        gs.advanceStep(gd);

        assertThat(gd.stack).isEmpty();
    }

    private List<com.github.laxika.magicalvibes.model.Card> cards(int count) {
        List<com.github.laxika.magicalvibes.model.Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new GrizzlyBears());
        }
        return cards;
    }
}
