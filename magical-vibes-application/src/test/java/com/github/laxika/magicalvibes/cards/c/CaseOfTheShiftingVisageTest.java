package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.s.SqueeGoblinNabob;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CaseOfTheShiftingVisage.class, GrizzlyBears.class, Naturalize.class, SqueeGoblinNabob.class})
class CaseOfTheShiftingVisageTest extends BaseCardTest {

    @Test
    @DisplayName("Surveils 1 at the beginning of your upkeep")
    void surveilsAtUpkeep() {
        harness.addToBattlefield(player1, new CaseOfTheShiftingVisage());
        var topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class))
                .isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("Solves at the beginning of the end step with fifteen graveyard cards")
    void solvesWithFifteenCardsInGraveyard() {
        Permanent casePermanent = harness.addToBattlefieldAndReturn(
                player1, new CaseOfTheShiftingVisage());
        setGraveyardSize(15);

        resolveEndStepTrigger();

        assertThat(casePermanent.isSolved()).isTrue();
    }

    @Test
    @DisplayName("Does not solve with fewer than fifteen graveyard cards")
    void doesNotSolveWithFewerThanFifteenCards() {
        Permanent casePermanent = harness.addToBattlefieldAndReturn(
                player1, new CaseOfTheShiftingVisage());
        setGraveyardSize(14);

        resolveEndStepTrigger();

        assertThat(casePermanent.isSolved()).isFalse();
    }

    @Test
    @DisplayName("Copies a nonlegendary creature spell as a token after solving")
    void copiesNonlegendaryCreatureSpellAsToken() {
        solveCase();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        List<Permanent> bears = findPermanents(player1, "Grizzly Bears");
        assertThat(bears).hasSize(2);
        assertThat(bears).anyMatch(permanent -> permanent.getCard().isToken());
        assertThat(bears).anyMatch(permanent -> !permanent.getCard().isToken());
    }

    @Test
    @DisplayName("Does not copy a legendary creature spell")
    void doesNotCopyLegendaryCreatureSpell() {
        solveCase();
        GrizzlyBears legendaryBears = new GrizzlyBears();
        legendaryBears.setSupertypes(Set.of(CardSupertype.LEGENDARY));
        harness.setHand(player1, List.of(legendaryBears));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).noneMatch(entry -> entry.isCopy());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void canLeaveSurveilledCardOnTop() {
        harness.addToBattlefield(player1, new CaseOfTheShiftingVisage());
        GrizzlyBears topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(topCard);
    }

    @Test
    void doesNotSurveilOnOpponentsUpkeep() {
        harness.addToBattlefield(player1, new CaseOfTheShiftingVisage());
        GrizzlyBears topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    void surveilWithEmptyLibraryNeedsNoChoice() {
        harness.addToBattlefield(player1, new CaseOfTheShiftingVisage());
        harness.setLibrary(player1, List.of());

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void doesNotSolveOnOpponentsEndStep() {
        Permanent casePermanent = harness.addToBattlefieldAndReturn(
                player1, new CaseOfTheShiftingVisage());
        setGraveyardSize(15);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(casePermanent.isSolved()).isFalse();
    }

    @Test
    void solveConditionIsCheckedAgainOnResolution() {
        Permanent casePermanent = harness.addToBattlefieldAndReturn(
                player1, new CaseOfTheShiftingVisage());
        setGraveyardSize(15);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        setGraveyardSize(14);
        resolveAllTriggers();

        assertThat(casePermanent.isSolved()).isFalse();
    }

    @Test
    void opponentsGraveyardDoesNotCountForSolving() {
        Permanent casePermanent = harness.addToBattlefieldAndReturn(
                player1, new CaseOfTheShiftingVisage());
        setGraveyardSize(14);
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(casePermanent.isSolved()).isFalse();
    }

    @Test
    void solvedCaseDoesNotTriggerToSolveAgain() {
        solveCase();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanent(player1, "Case of the Shifting Visage").isSolved()).isTrue();
    }

    @Test
    void doesNotCopyCreatureWhileUnsolved() {
        harness.addToBattlefield(player1, new CaseOfTheShiftingVisage());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(1);
        assertThat(findPermanent(player1, "Grizzly Bears").getCard().isToken()).isFalse();
    }

    @Test
    void doesNotCopyRealLegendaryCreature() {
        solveCase();
        harness.setHand(player1, List.of(new SqueeGoblinNabob()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Squee, Goblin Nabob")).hasSize(1);
        assertThat(findPermanent(player1, "Squee, Goblin Nabob").getCard().isToken()).isFalse();
    }

    @Test
    void doesNotCopyNoncreatureSpell() {
        solveCase();
        Permanent target = harness.addToBattlefieldAndReturn(
                player2, new CaseOfTheShiftingVisage());
        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, target.getId());

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        harness.assertNotOnBattlefield(player2, "Case of the Shifting Visage");
    }

    @Test
    void remainsSolvedAfterGraveyardDropsBelowThreshold() {
        solveCase();
        setGraveyardSize(0);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Case of the Shifting Visage").isSolved()).isTrue();
        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(2);
    }

    @Test
    void copyTriggerStillResolvesAfterCaseIsDestroyed() {
        solveCase();
        Permanent casePermanent = findPermanent(player1, "Case of the Shifting Visage");
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(2);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, casePermanent.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Case of the Shifting Visage");
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(2);
        assertThat(findPermanents(player1, "Grizzly Bears"))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1);
    }

    @Test
    void doesNotCopyOpponentsCreatureSpell() {
        solveCase();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(findPermanents(player2, "Grizzly Bears")).hasSize(1);
        assertThat(findPermanents(player1, "Grizzly Bears")).isEmpty();
    }

    private void solveCase() {
        harness.addToBattlefield(player1, new CaseOfTheShiftingVisage());
        setGraveyardSize(15);
        resolveEndStepTrigger();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void setGraveyardSize(int size) {
        harness.setGraveyard(player1, IntStream.range(0, size)
                .mapToObj(ignored -> (Card) new GrizzlyBears())
                .toList());
    }

    private void resolveEndStepTrigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
