package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WaterbendingLesson;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
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

    @Test
    void chapterICreatesFoodWhenLibraryHasFewerThanThreeCards() {
        harness.setLibrary(player1, List.of(new Forest()));
        addSaga(0);

        triggerChapter();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        harness.assertOnBattlefield(player1, "Food");
    }

    @Test
    void foodCanBeSacrificedForThreeLife() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        addSaga(0);
        triggerChapter();
        harness.passBothPriorities();
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.GREEN, 2);
        Permanent food = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Food"))
                .findFirst().orElseThrow();

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(food), null, null);

        harness.assertNotOnBattlefield(player1, "Food");
        harness.assertLife(player1, 10);
        harness.passBothPriorities();
        harness.assertLife(player1, 13);
    }

    @Test
    void chapterIICanChooseZeroTargets() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        addSaga(1);
        triggerChapter();

        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void chapterIICanChooseOnlyOneOfTwoCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        addSaga(1);
        triggerChapter();

        harness.handlePermanentChosen(player1, first.getId());
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).doesNotContain(first.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void chapterIIIDoesNotCountOpponentsGraveyard() {
        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new WaterbendingLesson()));
        addSaga(2);

        triggerChapter();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        harness.assertInGraveyard(player1, "Leaves from the Vine");
    }

    @Test
    void chapterIIIChecksGraveyardAtResolutionWhenMatchingCardIsAdded() {
        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        harness.setGraveyard(player1, List.of());
        addSaga(2);
        triggerChapter();
        assertThat(gd.stack).hasSize(1);

        harness.setGraveyard(player1, List.of(new WaterbendingLesson()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
    }

    @Test
    void chapterIIIChecksGraveyardAtResolutionWhenMatchingCardIsRemoved() {
        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        addSaga(2);
        triggerChapter();
        assertThat(gd.stack).hasSize(1);

        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        harness.assertInGraveyard(player1, "Leaves from the Vine");
    }

    @Test
    void chapterITriggersWhenSagaEnters() {
        harness.setHand(player1, List.of(new LeavesFromTheVine()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        harness.assertOnBattlefield(player1, "Food");
    }

    @Test
    void chapterIIIDrawsOnlyOneCardWithBothCreatureAndLesson() {
        Forest topCard = new Forest();
        Forest nextCard = new Forest();
        harness.setLibrary(player1, List.of(topCard, nextCard));
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new WaterbendingLesson()));
        addSaga(2);

        triggerChapter();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(topCard).doesNotContain(nextCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nextCard);
    }

    private Permanent addSaga(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new LeavesFromTheVine());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void triggerChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.passBothPriorities();
    }
}
