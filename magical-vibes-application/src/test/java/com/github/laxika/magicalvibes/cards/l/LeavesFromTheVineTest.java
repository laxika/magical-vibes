package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WaterbendingLesson;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LeavesFromTheVine.class, Forest.class, GrizzlyBears.class, WaterbendingLesson.class})
class LeavesFromTheVineTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I mills three cards and creates a Food token")
    void chapterIMillsAndCreatesFood() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        addSaga(0);

        triggerChapter();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        harness.assertOnBattlefield(player1, "Food");
    }

    @Test
    @DisplayName("Chapter II puts a +1/+1 counter on up to two creatures you control")
    void chapterIICountersUpToTwoControlledCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        addSaga(1);

        triggerChapter();

        PendingInteraction.PermanentChoice firstChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(firstChoice.validIds()).contains(first.getId(), second.getId())
                .doesNotContain(opponent.getId());
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Chapter III draws a card when your graveyard contains a creature")
    void chapterIIIDrawsForCreatureInGraveyard() {
        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        addSaga(2);

        triggerChapter();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
        harness.assertInGraveyard(player1, "Leaves from the Vine");
    }

    @Test
    @DisplayName("Chapter III draws a card when your graveyard contains a Lesson")
    void chapterIIIDrawsForLessonInGraveyard() {
        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        harness.setGraveyard(player1, List.of(new WaterbendingLesson()));
        addSaga(2);

        triggerChapter();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("Chapter III does not draw without a creature or Lesson in your graveyard")
    void chapterIIIDoesNotDrawWithoutMatchingGraveyardCard() {
        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        harness.setGraveyard(player1, List.of(new Forest()));
        addSaga(2);

        triggerChapter();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    private Permanent addSaga(int loreCounters) {
        Permanent saga = new Permanent(new LeavesFromTheVine());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        gd.playerBattlefields.get(player1.getId()).add(saga);
        return saga;
    }

    private void triggerChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
