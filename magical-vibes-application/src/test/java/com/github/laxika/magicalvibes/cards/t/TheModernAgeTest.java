package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.v.VectorGlider;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheModernAge.class, VectorGlider.class, GrizzlyBears.class, Shock.class})
class TheModernAgeTest extends BaseCardTest {

    @Test
    void chapterIDrawsThenDiscards() {
        harness.setHand(player1, List.of(new Shock()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addSagaWithLore(0);

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Shock");
    }

    @Test
    void chapterIIDrawsThenDiscards() {
        harness.setHand(player1, List.of(new Shock()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addSagaWithLore(1);

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Shock");
    }

    @Test
    void chapterIIITransformsIntoVectorGlider() {
        addSagaWithLore(2);

        advanceToNextChapter();
        harness.passBothPriorities();

        Permanent glider = findPermanent(player1, "Vector Glider");
        assertThat(glider).isNotNull();
        assertThat(glider.isTransformed()).isTrue();
    }

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheModernAge());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
