package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AirbendingLesson;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WalltopSentries.class, AirbendingLesson.class})
class WalltopSentriesTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 2 life when it dies with a Lesson card in the controller's graveyard")
    void diesWithLessonInGraveyardGainsLife() {
        harness.setLife(player1, 20);
        harness.setGraveyard(player1, List.of(new AirbendingLesson()));
        killSentries();

        resolveAllTriggers();

        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Does not gain life when it dies without a Lesson card in the controller's graveyard")
    void diesWithoutLessonInGraveyardDoesNotGainLife() {
        harness.setLife(player1, 20);
        harness.setGraveyard(player1, List.of(new WalltopSentries()));
        killSentries();

        resolveAllTriggers();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("The intervening-if is checked again when the death trigger resolves")
    void removingLessonBeforeResolutionPreventsLifeGain() {
        harness.setLife(player1, 20);
        harness.setGraveyard(player1, List.of(new AirbendingLesson()));
        killSentries();
        harness.setGraveyard(player1, List.of(new WalltopSentries()));

        resolveAllTriggers();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("A Lesson card in an opponent's graveyard does not count")
    void opponentLessonDoesNotCount() {
        harness.setLife(player1, 20);
        harness.setGraveyard(player2, List.of(new AirbendingLesson()));
        killSentries();

        resolveAllTriggers();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("A Lesson entering the graveyard after death cannot create a trigger")
    void lessonArrivingAfterDeathDoesNotGainLife() {
        harness.setLife(player1, 20);
        harness.setGraveyard(player1, List.of());
        killSentries();

        assertThat(gd.stack).isEmpty();
        harness.setGraveyard(player1, List.of(new AirbendingLesson()));
        resolveAllTriggers();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Multiple Lesson cards still yield only 2 life")
    void multipleLessonsGainOnlyTwoLife() {
        harness.setLife(player1, 20);
        harness.setGraveyard(player1, List.of(new AirbendingLesson(), new AirbendingLesson()));
        killSentries();

        resolveAllTriggers();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The Lesson present at resolution need not be the same card present at death")
    void replacementLessonStillAllowsLifeGain() {
        harness.setLife(player1, 20);
        harness.setGraveyard(player1, List.of(new AirbendingLesson()));
        killSentries();
        assertThat(gd.stack).hasSize(1);
        harness.setGraveyard(player1, List.of(new AirbendingLesson()));

        resolveAllTriggers();

        harness.assertLife(player1, 22);
    }

    private void killSentries() {
        Permanent sentries = harness.addToBattlefieldAndReturn(player1, new WalltopSentries());
        sentries.setMarkedDamage(3);
        harness.runStateBasedActions();
    }
}
