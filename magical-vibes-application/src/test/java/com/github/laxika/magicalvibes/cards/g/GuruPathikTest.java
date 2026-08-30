package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AvenShrine;
import com.github.laxika.magicalvibes.cards.f.FirebendingLesson;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
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

@CardUsed({GuruPathik.class, AvenShrine.class, FirebendingLesson.class, HillGiant.class,
        Shock.class, TheRiseOfSozin.class})
class GuruPathikTest extends BaseCardTest {

    @Test
    @DisplayName("ETB offers Lesson, Saga, and Shrine cards from the top five")
    void etbOffersLessonSagaAndShrine() {
        Card lesson = new FirebendingLesson();
        Card saga = new TheRiseOfSozin();
        Card shrine = new AvenShrine();
        Card shock = new Shock();
        Card forest = new Forest();
        setLibrary(List.of(lesson, saga, shrine, shock, forest));
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
        setLibrary(List.of(lesson, shock));
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
        harness.passBothPriorities();
        harness.passBothPriorities();

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

    private void setLibrary(List<Card> cards) {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(cards);
    }
}
