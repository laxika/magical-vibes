package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StampedingElkHerd.class, GrizzlyBears.class})
class StampedingElkHerdTest extends BaseCardTest {

    @Test
    @DisplayName("Gives your creatures trample when you attack with 8 total power")
    void givesYourCreaturesTrampleWithEnoughTotalPower() {
        Permanent herd = addCreatureReady(player1, new StampedingElkHerd());
        Permanent bear1 = addCreatureReady(player1, new GrizzlyBears());
        Permanent bear2 = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentsBear = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, herd, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bear1, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bear2, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentsBear, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Does not trigger when creatures you control have less than 8 total power")
    void doesNotTriggerWithInsufficientTotalPower() {
        Permanent herd = addCreatureReady(player1, new StampedingElkHerd());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, herd, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Trample wears off at end of turn")
    void trampleWearsOffAtEndOfTurn() {
        Permanent herd = addCreatureReady(player1, new StampedingElkHerd());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        resolveAllTriggers();
        assertThat(gqs.hasKeyword(gd, herd, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, herd, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.TRAMPLE)).isFalse();
    }
}
