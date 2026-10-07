package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GuidelightSynergist;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpotcycleScouter.class, GuidelightSynergist.class})
class SpotcycleScouterTest extends BaseCardTest {

    @Test
    @DisplayName("When Spotcycle Scouter enters, it offers scry 2")
    void etbOffersScryTwo() {
        harness.setHand(player1, List.of(new SpotcycleScouter()));
        addCastingMana();

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).hasSize(2);
    }

    @Test
    @DisplayName("Crew 1 makes Spotcycle Scouter a creature and taps the crew")
    void crewAnimatesVehicleAndTapsCrew() {
        Permanent vehicle = addVehicleReady(player1);
        Permanent crew = addCreatureReady(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
        assertThat(crew.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Spotcycle Scouter stops being a creature at end of turn")
    void crewAnimationResetsAtEndOfTurn() {
        Permanent vehicle = addVehicleReady(player1);
        addCreatureReady(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, vehicle)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, vehicle)).isFalse();
    }

    @Test
    void scryCanReorderBothCardsOnTop() {
        Card first = new SpotcycleScouter();
        Card second = new GuidelightSynergist();
        Card third = new SpotcycleScouter();
        harness.setLibrary(player1, List.of(first, second, third));
        castAndResolveEntry();

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, first, third);
    }

    @Test
    void scryCanKeepOneCardAndBottomTheOther() {
        Card first = new SpotcycleScouter();
        Card second = new GuidelightSynergist();
        Card third = new SpotcycleScouter();
        harness.setLibrary(player1, List.of(first, second, third));
        castAndResolveEntry();

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, third, first);
    }

    @Test
    void scryCanBottomBothCardsInChosenOrder() {
        Card first = new SpotcycleScouter();
        Card second = new GuidelightSynergist();
        Card third = new SpotcycleScouter();
        harness.setLibrary(player1, List.of(first, second, third));
        castAndResolveEntry();

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(1, 0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third, second, first);
    }

    @Test
    void scryWithOneCardUsesOnlyThatCard() {
        Card only = new GuidelightSynergist();
        harness.setLibrary(player1, List.of(only));
        castAndResolveEntry();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).containsExactly(only);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(only);
    }

    @Test
    void scryWithEmptyLibraryFinishesWithoutAChoice() {
        harness.setLibrary(player1, List.of());
        castAndResolveEntry();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void newlyEnteredCreatureCanCrewAndCostIsPaidBeforeResolution() {
        Permanent vehicle = addVehicleReady(player1);
        Permanent crew = harness.addToBattlefieldAndReturn(player1, new GuidelightSynergist());
        crew.setSummoningSick(true);

        harness.activateAbility(player1, 0, null, null);

        assertThat(crew.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, vehicle)).isFalse();
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
        assertThat(vehicle.isTapped()).isFalse();
    }

    @Test
    void canTapAdditionalCrewAfterMeetingThePowerRequirement() {
        Permanent vehicle = addVehicleReady(player1);
        Permanent first = addCreatureReady(player1);
        Permanent second = addCreatureReady(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, first.getId());
        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isFalse();
        harness.handlePermanentChosen(player1, second.getId());
        harness.passBothPriorities();

        assertThat(second.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
    }
    private void castAndResolveEntry() {
        harness.setHand(player1, List.of(new SpotcycleScouter()));
        addCastingMana();
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void addCastingMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private Permanent addVehicleReady(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new SpotcycleScouter());
        permanent.setSummoningSick(false);
        return permanent;
    }

    private Permanent addCreatureReady(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new GuidelightSynergist());
        permanent.setSummoningSick(false);
        return permanent;
    }
}
