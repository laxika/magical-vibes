package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FirebendingLesson;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.h.HondenOfSeeingWinds;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TheRiseOfSozin;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GuruPathik.class, HondenOfSeeingWinds.class, FirebendingLesson.class, HillGiant.class,
        Shock.class, TheRiseOfSozin.class, Forest.class})
class GuruPathikTest extends BaseCardTest {

    @Test
    @DisplayName("ETB offers Lesson, Saga, and Shrine cards from the top five")
    void etbOffersLessonSagaAndShrine() {
        Card lesson = new FirebendingLesson();
        Card saga = new TheRiseOfSozin();
        Card shrine = new HondenOfSeeingWinds();
        Card shock = new Shock();
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(lesson, saga, shrine, shock, forest));
        harness.setHand(player1, List.of());

        harness.enterBattlefieldAndReturn(player1, new GuruPathik());
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(
                lesson.getId(), saga.getId(), shrine.getId());
        assertThat(choice.maxCount()).isEqualTo(1);

        harness.handleMultipleCardsChosen(player1, List.of(saga.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(saga);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(lesson, shrine, shock, forest);
    }

    @Test
    @DisplayName("ETB may decline to reveal a matching card")
    void etbMayDecline() {
        Card lesson = new FirebendingLesson();
        Card shock = new Shock();
        harness.setLibrary(player1, List.of(lesson, shock));
        harness.setHand(player1, List.of());

        harness.enterBattlefieldAndReturn(player1, new GuruPathik());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(lesson, shock);
    }

    @Test
    @DisplayName("Casting a Lesson puts a counter on another creature you control")
    void lessonCastPutsCounterOnAnotherCreature() {
        Permanent guru = harness.addToBattlefieldAndReturn(player1, new GuruPathik());
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.setHand(player1, List.of(new FirebendingLesson()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, giant.getId());

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(giant.getId());
        harness.handlePermanentChosen(player1, giant.getId());
        resolveAllTriggers();

        assertThat(guru.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(giant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting an unrelated spell does not trigger Guru Pathik")
    void unrelatedSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new GuruPathik());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("ETB examines only five cards and puts the rest below the untouched library")
    void etbPreservesCardsBelowTopFive() {
        Card lesson = new FirebendingLesson();
        Card forest1 = new Forest();
        Card forest2 = new Forest();
        Card forest3 = new Forest();
        Card forest4 = new Forest();
        Card hiddenSaga = new TheRiseOfSozin();
        harness.setLibrary(player1, List.of(lesson, forest1, forest2, forest3, forest4, hiddenSaga));
        harness.setHand(player1, List.of());

        harness.enterBattlefieldAndReturn(player1, new GuruPathik());
        resolveAllTriggers();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactly(lesson.getId());
        harness.handleMultipleCardsChosen(player1, List.of(lesson.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(lesson);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(hiddenSaga);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(
                hiddenSaga, forest1, forest2, forest3, forest4);
    }

    @Test
    @DisplayName("ETB with no matching card puts every examined card back without a choice")
    void etbWithNoMatchingCard() {
        Card forest = new Forest();
        Card shock = new Shock();
        harness.setLibrary(player1, List.of(forest, shock));
        harness.setHand(player1, List.of());

        harness.enterBattlefieldAndReturn(player1, new GuruPathik());
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(forest, shock);
    }

    @Test
    @DisplayName("ETB with an empty library finishes without requiring a choice")
    void etbWithEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of());

        harness.enterBattlefieldAndReturn(player1, new GuruPathik());
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Casting a Saga triggers before the Saga resolves")
    void sagaCastPutsCounterOnAnotherCreature() {
        harness.addToBattlefield(player1, new GuruPathik());
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.setHand(player1, List.of(new TheRiseOfSozin()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castEnchantment(player1, 0);
        harness.handlePermanentChosen(player1, giant.getId());
        harness.passBothPriorities();

        assertThat(giant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(giant);
    }

    @Test
    @DisplayName("Casting a Shrine targets only another creature controlled by Guru's controller")
    void shrineCastFiltersTargets() {
        Permanent guru = harness.addToBattlefieldAndReturn(player1, new GuruPathik());
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opponentGiant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new HondenOfSeeingWinds()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castEnchantment(player1, 0);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(giant.getId());
        harness.handlePermanentChosen(player1, giant.getId());
        resolveAllTriggers();

        assertThat(giant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(guru.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponentGiant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(forest.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An opponent's Lesson does not trigger Guru Pathik")
    void opponentLessonDoesNotTrigger() {
        harness.addToBattlefield(player1, new GuruPathik());
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.setHand(player2, List.of(new FirebendingLesson()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, giant.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(giant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Casting a Lesson with no other friendly creature does not target Guru itself")
    void noOtherFriendlyCreature() {
        Permanent guru = harness.addToBattlefieldAndReturn(player1, new GuruPathik());
        Permanent opponentGiant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new FirebendingLesson()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, opponentGiant.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        resolveAllTriggers();
        assertThat(guru.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponentGiant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
