package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.l.LizardConnorssCurse;
import com.github.laxika.magicalvibes.cards.m.MaryJaneWatson;
import com.github.laxika.magicalvibes.model.ManaColor;
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

@CardUsed({DoctorOctopusMasterPlanner.class, DocOckSinisterScientist.class, MaryJaneWatson.class, LizardConnorssCurse.class})
class DoctorOctopusMasterPlannerTest extends BaseCardTest {

    @Test
    @DisplayName("Other Villains you control get +2/+2")
    void boostsOtherVillainsYouControl() {
        Permanent doctor = harness.addToBattlefieldAndReturn(player1, new DoctorOctopusMasterPlanner());
        Permanent ownVillain = harness.addToBattlefieldAndReturn(player1, new DocOckSinisterScientist());
        Permanent ownNonVillain = harness.addToBattlefieldAndReturn(player1, new MaryJaneWatson());
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

    @Test
    void drawsEightCardsFromAnEmptyHand() {
        harness.addToBattlefield(player1, new DoctorOctopusMasterPlanner());
        harness.setHand(player1, List.of());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(8);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 8);
    }

    @Test
    void recalculatesDrawAmountWhenHandSizeIncreasesBeforeResolution() {
        harness.addToBattlefield(player1, new DoctorOctopusMasterPlanner());
        harness.setHand(player1, cards(5));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);
        harness.setHand(player1, cards(7));
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(8);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
    }

    @Test
    void recalculatesDrawAmountWhenHandSizeDecreasesBeforeResolution() {
        harness.addToBattlefield(player1, new DoctorOctopusMasterPlanner());
        harness.setHand(player1, cards(5));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);
        harness.setHand(player1, cards(2));
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(8);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 6);
    }

    @Test
    void doesNotDrawWhenHandReachesEightBeforeResolution() {
        harness.addToBattlefield(player1, new DoctorOctopusMasterPlanner());
        harness.setHand(player1, cards(5));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);
        harness.setHand(player1, cards(8));
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(8);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotTriggerDuringOpponentsEndStep() {
        harness.addToBattlefield(player1, new DoctorOctopusMasterPlanner());
        harness.setHand(player1, cards(5));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.passUntil(player2, TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(5);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore);
    }

    @Test
    void doesNotRaiseOpponentsMaximumHandSize() {
        harness.addToBattlefield(player1, new DoctorOctopusMasterPlanner());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player2, cards(9));

        gs.advanceStep(gd);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount())
                .isEqualTo(2);
    }
    @Test
    void maximumHandSizeReturnsToSevenWhenDoctorLosesAllAbilities() {
        Permanent doctor = harness.addToBattlefieldAndReturn(player1, new DoctorOctopusMasterPlanner());
        harness.setHand(player1, List.of(new LizardConnorssCurse()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0, List.of(doctor.getId()));
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, doctor)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, doctor)).isEqualTo(4);
        harness.setHand(player1, cards(9));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);

        gs.advanceStep(gd);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount())
                .isEqualTo(2);
    }
    private List<com.github.laxika.magicalvibes.model.Card> cards(int count) {
        List<com.github.laxika.magicalvibes.model.Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new MaryJaneWatson());
        }
        return cards;
    }
}
