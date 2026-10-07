package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.ColossodonYearling;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StampedingElkHerd.class, ColossodonYearling.class})
class StampedingElkHerdTest extends BaseCardTest {

    @Test
    @DisplayName("Gives your creatures trample when you attack with 8 total power")
    void givesYourCreaturesTrampleWithEnoughTotalPower() {
        Permanent herd = addCreatureReady(player1, new StampedingElkHerd());
        Permanent bear1 = addCreatureReady(player1, new ColossodonYearling());
        Permanent bear2 = addCreatureReady(player1, new ColossodonYearling());
        Permanent opponentsBear = addCreatureReady(player2, new ColossodonYearling());

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
        Permanent bear = addCreatureReady(player1, new ColossodonYearling());

        declareAttackers(List.of(0));

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, herd, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Trample wears off at end of turn")
    void trampleWearsOffAtEndOfTurn() {
        Permanent herd = addCreatureReady(player1, new StampedingElkHerd());
        Permanent bear = addCreatureReady(player1, new ColossodonYearling());
        addCreatureReady(player1, new ColossodonYearling());

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

    @Test
    @DisplayName("Formidable counts modified power and triggers at exactly eight")
    void triggersAtExactlyEightTotalPower() {
        Permanent herd = addCreatureReady(player1, new StampedingElkHerd());
        Permanent yearling = addCreatureReady(player1, new ColossodonYearling());
        yearling.setPowerModifier(1);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, herd, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, yearling, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Formidable is checked again when the attack trigger resolves")
    void doesNothingIfTotalPowerDropsBeforeResolution() {
        Permanent herd = addCreatureReady(player1, new StampedingElkHerd());
        Permanent yearling = addCreatureReady(player1, new ColossodonYearling());
        yearling.setPowerModifier(1);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        assertThat(gd.stack).hasSize(1);
        yearling.setPowerModifier(0);
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, herd, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, yearling, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Creatures entering before resolution gain trample, but later creatures do not")
    void grantsTrampleOnlyToCreaturesPresentAtResolution() {
        addCreatureReady(player1, new StampedingElkHerd());
        addCreatureReady(player1, new ColossodonYearling());
        addCreatureReady(player1, new ColossodonYearling());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        assertThat(gd.stack).hasSize(1);
        Permanent beforeResolution = addCreatureReady(player1, new ColossodonYearling());
        resolveAllTriggers();
        Permanent afterResolution = addCreatureReady(player1, new ColossodonYearling());

        assertThat(gqs.hasKeyword(gd, beforeResolution, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, afterResolution, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Another creature attacking does not trigger a nonattacking Herd")
    void doesNotTriggerWhenOnlyAnotherCreatureAttacks() {
        Permanent herd = addCreatureReady(player1, new StampedingElkHerd());
        Permanent yearling = addCreatureReady(player1, new ColossodonYearling());
        addCreatureReady(player1, new ColossodonYearling());

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, herd, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, yearling, Keyword.TRAMPLE)).isFalse();
    }
}
