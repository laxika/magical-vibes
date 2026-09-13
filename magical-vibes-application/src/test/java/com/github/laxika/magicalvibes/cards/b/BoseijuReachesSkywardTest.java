package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({BoseijuReachesSkyward.class, BranchOfBoseiju.class, Forest.class, GrizzlyBears.class, Plains.class})
class BoseijuReachesSkywardTest extends BaseCardTest {

    @Test
    void chapterISearchesForUpToTwoBasicForests() {
        Forest firstForest = new Forest();
        Plains plains = new Plains();
        Forest secondForest = new Forest();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(firstForest, plains, secondForest, bears));
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
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(plains, bears);
    }

    @Test
    void chapterIIPutsUpToOneTargetLandFromGraveyardOnTopOfLibrary() {
        Forest land = new Forest();
        GrizzlyBears creature = new GrizzlyBears();
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

        Permanent branch = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof BranchOfBoseiju)
                .findFirst()
                .orElseThrow();
        assertThat(branch.isTransformed()).isTrue();
        assertThat(gqs.getEffectivePower(gd, branch)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, branch)).isEqualTo(1);

        harness.addToBattlefield(player1, new Forest());
        assertThat(gqs.getEffectivePower(gd, branch)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, branch)).isEqualTo(2);
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
