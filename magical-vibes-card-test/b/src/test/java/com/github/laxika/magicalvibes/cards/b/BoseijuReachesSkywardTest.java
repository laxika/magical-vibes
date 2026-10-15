package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FangOfShigeki;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BoseijuReachesSkyward.class, BranchOfBoseiju.class, Forest.class, FangOfShigeki.class, Plains.class})
class BoseijuReachesSkywardTest extends BaseCardTest {

    @Test
    void chapterISearchesForUpToTwoBasicForests() {
        Forest firstForest = new Forest();
        Plains plains = new Plains();
        Forest secondForest = new Forest();
        FangOfShigeki creature = new FangOfShigeki();
        harness.setLibrary(player1, List.of(firstForest, plains, secondForest, creature));
        int handBefore = gd.playerHands.get(player1.getId()).size();
        addSagaWithLore(0);

        triggerChapter();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.HAND);
        assertThat(search.params().cards()).containsExactly(firstForest, secondForest);
        assertThat(search.params().remainingCount()).isEqualTo(2);
        assertThat(search.params().reveals()).isTrue();

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2)
                .contains(firstForest, secondForest);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(plains, creature);
    }

    @Test
    void chapterIIPutsUpToOneTargetLandFromGraveyardOnTopOfLibrary() {
        Forest land = new Forest();
        FangOfShigeki creature = new FangOfShigeki();
        harness.setGraveyard(player1, List.of(land, creature));
        Card oldTop = new Plains();
        harness.setLibrary(player1, List.of(oldTop));
        addSagaWithLore(1);

        triggerChapter();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(land.getId());
        harness.handleMultipleCardsChosen(player1, List.of(land.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land, oldTop);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creature);
    }

    @Test
    void chapterIIITransformsIntoBranchOfBoseijuUnderYourControl() {
        harness.addToBattlefield(player1, new Forest());
        addSagaWithLore(2);

        triggerChapter();
        harness.passBothPriorities();

        Permanent branch = findPermanent(player1, "Branch of Boseiju");
        assertThat(branch.isTransformed()).isTrue();
        assertThat(gqs.getEffectivePower(gd, branch)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, branch)).isEqualTo(1);

        harness.addToBattlefield(player1, new Forest());
        assertThat(gqs.getEffectivePower(gd, branch)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, branch)).isEqualTo(2);
    }

    @Test
    void enteringSagaTriggersChapterIWithoutWaitingForAnotherTurn() {
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        harness.castFromHand(player1, new BoseijuReachesSkyward(), "{3}{G}");

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(forest);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void chapterICanFindNoForestsEvenWhenTwoAreAvailable() {
        Forest first = new Forest();
        Forest second = new Forest();
        harness.setLibrary(player1, List.of(first, second));
        int handBefore = gd.playerHands.get(player1.getId()).size();
        addSagaWithLore(0);

        triggerChapter();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(first, second);
    }

    @Test
    void chapterICanStopAfterFindingOneForest() {
        Forest first = new Forest();
        Forest second = new Forest();
        harness.setLibrary(player1, List.of(first, second));
        int handBefore = gd.playerHands.get(player1.getId()).size();
        addSagaWithLore(0);

        triggerChapter();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1).contains(first);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second);
    }

    @Test
    void chapterIICanChooseNoTargetAndDoesNotOfferOpponentsLands() {
        Forest ownLand = new Forest();
        Forest opposingLand = new Forest();
        Plains oldTop = new Plains();
        harness.setGraveyard(player1, List.of(ownLand));
        harness.setGraveyard(player2, List.of(opposingLand));
        harness.setLibrary(player1, List.of(oldTop));
        addSagaWithLore(1);

        triggerChapter();
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(ownLand.getId());
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(oldTop);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(ownLand);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opposingLand);
    }

    @Test
    void returnedBranchCannotAttackDuringTheTurnItEnters() {
        harness.addToBattlefield(player1, new Forest());
        addSagaWithLore(2);

        triggerChapter();
        harness.passBothPriorities();

        Permanent branch = findPermanent(player1, "Branch of Boseiju");
        assertThat(als.canAttack(gd, branch, player1.getId())).isFalse();
    }

    @Test
    void chapterIIIReturnsToTheTriggerControllerAfterSagaChangesControl() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        Permanent saga = addSagaWithLore(2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, harness::passBothPriorities);
        assertThat(gd.stack).hasSize(1);

        gd.playerBattlefields.get(player1.getId()).remove(saga);
        gd.playerBattlefields.get(player2.getId()).add(saga);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Branch of Boseiju");
        harness.assertNotOnBattlefield(player2, "Branch of Boseiju");
    }

    @Test
    void branchCountsOnlyItsControllersLandsAndIncludesNonForests() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player2, new Forest());
        addSagaWithLore(2);

        triggerChapter();
        harness.passBothPriorities();

        Permanent branch = findPermanent(player1, "Branch of Boseiju");
        assertThat(gqs.getEffectivePower(gd, branch)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, branch)).isEqualTo(2);
    }

    @Test
    void branchDiesWhenItReturnsWithNoLandsUnderItsControllersControl() {
        harness.addToBattlefield(player2, new Forest());
        addSagaWithLore(2);

        triggerChapter();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Branch of Boseiju");
        harness.assertInGraveyard(player1, "Boseiju Reaches Skyward");
    }

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new BoseijuReachesSkyward());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void triggerChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
