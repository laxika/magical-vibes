package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.cards.p.Panharmonicon;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NylaShirshuSleuth.class, GrizzlyBears.class, Panharmonicon.class})
class NylaShirshuSleuthTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles up to one creature card from your graveyard, then you lose life and create Clues equal to its mana value")
    void etbExilesCreatureLosesLifeAndCreatesCluesEqualToManaValue() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.castFromHand(player1, new NylaShirshuSleuth(), "{4}{B}");
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(bears.getId());
        assertThat(choice.minCount()).isZero();
        assertThat(choice.maxCount()).isEqualTo(1);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        resolveAllTriggers();

        Permanent nyla = findPermanent(player1, "Nyla, Shirshu Sleuth");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.getCardsExiledByPermanent(nyla.getId())).containsExactly(bears);
        assertThat(findPermanents(player1, "Clue")).hasSize(2);
    }

    @Test
    @DisplayName("Choosing no target exiles nothing, loses no life and creates no Clues")
    void etbMayDeclineAndCreatesNoClues() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.castFromHand(player1, new NylaShirshuSleuth(), "{4}{B}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of());
        resolveAllTriggers();

        Permanent nyla = findPermanent(player1, "Nyla, Shirshu Sleuth");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.getCardsExiledByPermanent(nyla.getId())).isEmpty();
        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    @DisplayName("A target that left the graveyard before resolution loses no life and creates no Clues")
    void targetLeavingGraveyardLosesNoLifeAndCreatesNoClues() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.castFromHand(player1, new NylaShirshuSleuth(), "{4}{B}");
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.setGraveyard(player1, List.of());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    @DisplayName("The enter trigger can't target a creature card in an opponent's graveyard")
    void cannotTargetOpponentsGraveyard() {
        Card own = new GrizzlyBears();
        Card opponents = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(own));
        harness.setGraveyard(player2, List.of(opponents));
        harness.castFromHand(player1, new NylaShirshuSleuth(), "{4}{B}");
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(own.getId());
    }

    @Test
    @DisplayName("At your end step with no Clues, returns a card exiled with Nyla to its owner's hand")
    void endStepReturnsTargetCardExiledWithNylaWhenNoCluesAreControlled() {
        Permanent nyla = addCreatureReady(player1, new NylaShirshuSleuth());
        Card bears = new GrizzlyBears();
        gd.addToExile(player1.getId(), bears, nyla.getId());

        advanceToEndStep();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(bears.getId());
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(bears);
        assertThat(gd.findExiledCard(bears.getId())).isNull();
    }

    @Test
    @DisplayName("At your end step while you control a Clue, nothing is returned")
    void endStepDoesNotReturnCardWhileAClueIsControlled() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.castFromHand(player1, new NylaShirshuSleuth(), "{4}{B}");
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        resolveAllTriggers();

        Permanent nyla = findPermanent(player1, "Nyla, Shirshu Sleuth");
        advanceToEndStep();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.getCardsExiledByPermanent(nyla.getId())).containsExactly(bears);
    }

    @Test
    @DisplayName("Each enter trigger counts only the mana value of the card it exiled")
    void additionalEnterTriggerUsesOnlyTheCardExiledByThatTrigger() {
        harness.addToBattlefield(player1, new Panharmonicon());
        Card first = new NylaShirshuSleuth();
        Card second = new NylaShirshuSleuth();
        harness.setGraveyard(player1, List.of(first, second));
        harness.castFromHand(player1, new NylaShirshuSleuth(), "{4}{B}");
        harness.passBothPriorities();

        harness.handleMultipleCardsChosen(player1, List.of(first.getId()));
        resolveAllTriggers();
        harness.handleMultipleCardsChosen(player1, List.of(second.getId()));
        resolveAllTriggers();

        Permanent nyla = findPermanent(player1, "Nyla, Shirshu Sleuth");
        assertThat(gd.getCardsExiledByPermanent(nyla.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(10);
        assertThat(findPermanents(player1, "Clue")).hasSize(10);
    }

    @Test
    @DisplayName("With no creature card in your graveyard, the enter trigger does nothing")
    void enterWithNoCreatureCardsInGraveyardHasNoFollowUpEffect() {
        harness.castFromHand(player1, new NylaShirshuSleuth(), "{4}{B}");
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The return ability does not trigger during an opponent's end step")
    void returnAbilityDoesNotTriggerDuringOpponentsEndStep() {
        Permanent nyla = addCreatureReady(player1, new NylaShirshuSleuth());
        Card exiled = new NylaShirshuSleuth();
        gd.addToExile(player1.getId(), exiled, nyla.getId());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getCardsExiledByPermanent(nyla.getId())).containsExactly(exiled);
    }
    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
