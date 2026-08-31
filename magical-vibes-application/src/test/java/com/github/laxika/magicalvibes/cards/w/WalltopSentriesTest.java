package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AirbendingLesson;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({WalltopSentries.class, AirbendingLesson.class, GrizzlyBears.class})
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
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
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
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

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

    private void killSentries() {
        Permanent sentries = harness.addToBattlefieldAndReturn(player1, new WalltopSentries());
        sentries.setMarkedDamage(3);
        harness.runStateBasedActions();
    }
}
