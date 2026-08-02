package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DaringSkyjekTest extends BaseCardTest {

    @Test
    @DisplayName("Does not gain flying when attacking with fewer than two other creatures")
    void noFlyingWithFewerThanTwoOtherAttackers() {
        Permanent skyjek = addCreatureReady(player1, new DaringSkyjek());
        addCreatureReady(player1, new DaringSkyjek());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, skyjek, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Gains flying when attacking with two other creatures")
    void gainsFlyingWithTwoOtherAttackers() {
        Permanent skyjek = addCreatureReady(player1, new DaringSkyjek());
        addCreatureReady(player1, new DaringSkyjek());
        addCreatureReady(player1, new DaringSkyjek());

        declareAttackers(player1, List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, skyjek, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Flying wears off at end of turn")
    void flyingWearsOffAtEndOfTurn() {
        Permanent skyjek = addCreatureReady(player1, new DaringSkyjek());
        addCreatureReady(player1, new DaringSkyjek());
        addCreatureReady(player1, new DaringSkyjek());

        declareAttackers(player1, List.of(0, 1, 2));
        resolveAllTriggers();
        assertThat(gqs.hasKeyword(gd, skyjek, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, skyjek, Keyword.FLYING)).isFalse();
    }
}
